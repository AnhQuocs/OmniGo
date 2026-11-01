import { useState, useRef, useEffect, useCallback } from 'react';
import toast from 'react-hot-toast';

export const useUndoDelete = ({
  onCommitDelete,
  onRestoreItem,
  itemType = 'mục',
}) => {
  const [pendingItem, setPendingItem] = useState(null);
  const [secondsLeft, setSecondsLeft] = useState(5);
  const [progressPercent, setProgressPercent] = useState(100);

  const timerRef = useRef(null);
  const intervalRef = useRef(null);
  const pendingItemRef = useRef(null);
  const startTimeRef = useRef(null);

  pendingItemRef.current = pendingItem;

  const clearTimers = useCallback(() => {
    if (timerRef.current) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
  }, []);

  const commitPending = useCallback(async (itemToCommit) => {
    clearTimers();
    setPendingItem(null);
    setProgressPercent(100);
    if (!itemToCommit) return;

    try {
      await onCommitDelete(itemToCommit);
      toast.success(`Đã xóa vĩnh viễn ${itemType} "${itemToCommit.name || itemToCommit.code || ''}"`);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Lỗi khi xóa';
      toast.error(msg);
      // Restore on failure
      if (onRestoreItem) {
        onRestoreItem(itemToCommit);
      }
    }
  }, [clearTimers, onCommitDelete, onRestoreItem, itemType]);

  const startDeleteWithUndo = useCallback((item) => {
    // If there is an existing pending item, commit it immediately before queueing next
    if (pendingItemRef.current) {
      commitPending(pendingItemRef.current);
    }

    setPendingItem(item);
    setSecondsLeft(5);
    setProgressPercent(100);

    const DURATION = 5000;
    startTimeRef.current = Date.now();

    // High frequency interval (40ms ~ 25fps) for ultra-smooth circular countdown animation
    intervalRef.current = setInterval(() => {
      const elapsed = Date.now() - startTimeRef.current;
      const remaining = Math.max(0, DURATION - elapsed);
      const percent = (remaining / DURATION) * 100;

      setProgressPercent(percent);
      setSecondsLeft(Math.max(1, Math.ceil(remaining / 1000)));

      if (remaining <= 0) {
        if (intervalRef.current) {
          clearInterval(intervalRef.current);
          intervalRef.current = null;
        }
      }
    }, 40);

    // 5-second timer to execute the actual delete
    timerRef.current = setTimeout(() => {
      const itemToCommit = pendingItemRef.current;
      commitPending(itemToCommit);
    }, DURATION);
  }, [commitPending]);

  const handleUndo = useCallback(() => {
    const itemToRestore = pendingItemRef.current;
    clearTimers();
    setPendingItem(null);
    setProgressPercent(100);

    if (itemToRestore) {
      if (onRestoreItem) {
        onRestoreItem(itemToRestore);
      }
      toast.success(`Đã hoàn tác, khôi phục lại ${itemType} "${itemToRestore.name || itemToRestore.code || ''}"`);
    }
  }, [clearTimers, onRestoreItem, itemType]);

  const handleDismiss = useCallback(() => {
    // User clicked X: commit delete immediately
    if (pendingItemRef.current) {
      commitPending(pendingItemRef.current);
    }
  }, [commitPending]);

  // Flush any pending delete if user unmounts page
  useEffect(() => {
    return () => {
      if (pendingItemRef.current) {
        onCommitDelete(pendingItemRef.current).catch(() => {});
      }
      clearTimers();
    };
  }, []);

  return {
    pendingItem,
    secondsLeft,
    progressPercent,
    startDeleteWithUndo,
    handleUndo,
    handleDismiss,
    isPendingDelete: (idOrCode) => {
      if (!pendingItem) return false;
      return pendingItem.id === idOrCode || pendingItem.code === idOrCode;
    },
  };
};

export default useUndoDelete;
