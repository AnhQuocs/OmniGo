import { createTheme } from '@mui/material/styles';

export const getAppTheme = (mode = 'dark') => {
  const isDark = mode === 'dark';

  return createTheme({
    palette: {
      mode,
      background: {
        default: isDark ? '#0f1724' : '#f0f3f8',
        paper: isDark ? '#172334' : '#ffffff',
      },
      primary: {
        main: '#008cff',
        light: '#33a3ff',
        dark: '#0070cc',
        contrastText: '#ffffff',
      },
      secondary: {
        main: '#ff6633',
        contrastText: '#ffffff',
      },
      text: {
        primary: isDark ? '#ffffff' : '#1e293b',
        secondary: isDark ? '#94a3b8' : '#64748b',
        disabled: isDark ? '#64748b' : '#94a3b8',
      },
      divider: isDark ? 'rgba(255, 255, 255, 0.08)' : '#e2e8f0',
      success: {
        main: '#15ca20',
        contrastText: '#ffffff',
      },
      error: {
        main: '#ff3366',
        contrastText: '#ffffff',
      },
      warning: {
        main: '#ffc107',
        contrastText: '#1e293b',
      },
    },
    typography: {
      fontFamily: ['"Open Sans"', '"Poppins"', '"Segoe UI"', 'Roboto', 'sans-serif'].join(','),
      h4: {
        fontFamily: ['"Poppins"', '"Open Sans"', 'sans-serif'].join(','),
        fontSize: '1.65rem',
        fontWeight: 700,
        color: isDark ? '#ffffff' : '#1e293b',
      },
      h5: {
        fontFamily: ['"Poppins"', '"Open Sans"', 'sans-serif'].join(','),
        fontSize: '1.35rem',
        fontWeight: 600,
        color: isDark ? '#ffffff' : '#1e293b',
      },
      h6: {
        fontFamily: ['"Poppins"', '"Open Sans"', 'sans-serif'].join(','),
        fontSize: '1.05rem',
        fontWeight: 600,
        color: isDark ? '#ffffff' : '#1e293b',
      },
      body1: {
        fontSize: '0.92rem',
        color: isDark ? '#e2e8f0' : '#1e293b',
        lineHeight: 1.5,
      },
      body2: {
        fontSize: '0.85rem',
        color: isDark ? '#94a3b8' : '#64748b',
        lineHeight: 1.4,
      },
      subtitle1: {
        fontSize: '0.9rem',
        color: isDark ? '#cbd5e1' : '#475569',
        fontWeight: 600,
      },
      subtitle2: {
        fontSize: '0.8rem',
        color: isDark ? '#94a3b8' : '#64748b',
        fontWeight: 500,
      },
      button: {
        textTransform: 'none',
        fontWeight: 600,
        fontSize: '0.88rem',
      },
    },
    shape: {
      borderRadius: 10,
    },
    components: {
      MuiCssBaseline: {
        styleOverrides: {
          body: {
            backgroundColor: isDark ? '#0f1724' : '#f0f3f8',
            color: isDark ? '#ffffff' : '#1e293b',
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
            backgroundColor: isDark ? '#172334' : '#ffffff',
            border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : '#e2e8f0'}`,
            boxShadow: isDark ? '0 2px 10px rgba(0, 0, 0, 0.3)' : '0 2px 8px rgba(0, 0, 0, 0.04)',
            borderRadius: 12,
          },
        },
      },
      MuiAppBar: {
        styleOverrides: {
          root: {
            backgroundColor: isDark ? '#131d2c' : '#ffffff',
            color: isDark ? '#ffffff' : '#1e293b',
            borderBottom: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : '#e2e8f0'}`,
            boxShadow: 'none',
          },
        },
      },
      MuiDrawer: {
        styleOverrides: {
          paper: {
            backgroundColor: isDark ? '#111927' : '#ffffff',
            color: isDark ? '#ffffff' : '#1e293b',
            borderRight: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : '#e2e8f0'}`,
          },
        },
      },
      MuiTableHead: {
        styleOverrides: {
          root: {
            backgroundColor: isDark ? '#131d2c' : '#f8fafc',
            '& .MuiTableCell-root': {
              color: isDark ? '#cbd5e1' : '#475569',
              fontWeight: 700,
              fontSize: '0.82rem',
              textTransform: 'uppercase',
              letterSpacing: '0.04em',
              borderBottom: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : '#e2e8f0'}`,
            },
          },
        },
      },
      MuiTableCell: {
        styleOverrides: {
          root: {
            fontSize: '0.9rem',
            color: isDark ? '#e2e8f0' : '#1e293b',
            borderBottom: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.05)' : '#f1f5f9'}`,
            padding: '13px 16px',
          },
        },
      },
      MuiButton: {
        styleOverrides: {
          root: {
            borderRadius: 8,
            padding: '7px 18px',
            boxShadow: 'none',
            '&:hover': {
              boxShadow: 'none',
            },
          },
          containedPrimary: {
            backgroundColor: '#008cff',
            color: '#ffffff',
            '&:hover': {
              backgroundColor: '#0070cc',
            },
          },
          outlined: {
            borderColor: isDark ? 'rgba(255, 255, 255, 0.15)' : '#cbd5e1',
            color: isDark ? '#ffffff' : '#1e293b',
            '&:hover': {
              borderColor: '#008cff',
              backgroundColor: isDark ? 'rgba(0, 140, 255, 0.08)' : 'rgba(0, 140, 255, 0.04)',
            },
          },
        },
      },
      MuiTextField: {
        defaultProps: {
          size: 'small',
        },
      },
      MuiOutlinedInput: {
        styleOverrides: {
          root: {
            backgroundColor: isDark ? '#131d2c' : '#ffffff',
            borderRadius: 8,
            fontSize: '0.9rem',
            '& fieldset': {
              borderColor: isDark ? 'rgba(255, 255, 255, 0.12)' : '#cbd5e1',
            },
            '&:hover fieldset': {
              borderColor: isDark ? 'rgba(255, 255, 255, 0.25)' : '#94a3b8',
            },
            '&.Mui-focused fieldset': {
              borderColor: '#008cff',
              borderWidth: '1.5px',
            },
          },
        },
      },
    },
  });
};

export default getAppTheme;
