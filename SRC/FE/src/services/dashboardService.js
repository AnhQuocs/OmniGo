import { get } from './api';

export const dashboardService = {
  /**
   * Lấy số liệu tổng quan hệ thống thời gian thực
   * // TODO: Replace with real API endpoint when BE provides aggregated dashboard telemetry
   */
  getSystemMetrics: async () => {
    try {
      // If BE provides a metrics endpoint:
      const response = await get('/api/v1/dashboard/metrics');
      return response;
    } catch {
      // Mock metrics matching real multi-service operational flow
      return {
        activeDrivers: 1284,
        pendingBookings: 46,
        completedTripsToday: 8920,
        totalGmvToday: 428500000,
        growthRate: 12.5,
        hourlyTrend: [
          { time: '06:00', rides: 120, drivers: 450, revenue: 4200000 },
          { time: '08:00', rides: 580, drivers: 890, revenue: 21500000 },
          { time: '10:00', rides: 340, drivers: 750, revenue: 14200000 },
          { time: '12:00', rides: 460, drivers: 810, revenue: 18900000 },
          { time: '14:00', rides: 310, drivers: 720, revenue: 12300000 },
          { time: '17:00', rides: 740, drivers: 1100, revenue: 34800000 },
          { time: '19:00', rides: 680, drivers: 1050, revenue: 29500000 },
          { time: '21:00', rides: 410, drivers: 680, revenue: 16200000 },
        ],
        serviceDistribution: [
          { name: 'OmniBike', value: 48, count: 4280 },
          { name: 'OmniCar 4C', value: 32, count: 2854 },
          { name: 'OmniCar 7C', value: 12, count: 1070 },
          { name: 'OmniExpress', value: 8, count: 716 },
        ],
      };
    }
  },
};

export default dashboardService;
