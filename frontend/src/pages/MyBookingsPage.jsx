import { useQuery } from '@tanstack/react-query';
import { Table, Typography, Alert, Tag, Button, Space } from 'antd';
import { Link } from 'react-router-dom';
import dayjs from 'dayjs';
import { fetchMyBookings } from '../api/bookings';
import { errorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';

export default function MyBookingsPage() {
  const { user } = useAuth();

  const { data, isLoading, isError, error, refetch, isFetching } = useQuery({
    queryKey: ['my-bookings', user.email],
    queryFn: () => fetchMyBookings(user.email),
  });

  const columns = [
    { title: 'Reference', dataIndex: 'bookingReference', key: 'ref' },
    { title: 'Event', dataIndex: 'eventName', key: 'event' },
    { title: 'Qty', dataIndex: 'quantity', key: 'qty', width: 70 },
    {
      title: 'Total',
      dataIndex: 'totalAmount',
      key: 'total',
      render: (v) => `£${Number(v).toFixed(2)}`,
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (s) => <Tag color={s === 'CONFIRMED' ? 'green' : 'default'}>{s}</Tag>,
    },
    {
      title: 'Booked at',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (v) => dayjs(v).format('DD MMM YYYY, HH:mm'),
    },
  ];

  return (
    <div>
      <Space style={{ justifyContent: 'space-between', width: '100%', marginBottom: 12 }} wrap>
        <Typography.Title level={3} style={{ margin: 0 }}>My Bookings</Typography.Title>
        <Button onClick={() => refetch()} loading={isFetching}>Refresh</Button>
      </Space>

      {isError && <Alert type="error" showIcon message={errorMessage(error, 'Failed to load bookings')} />}

      <Table
        rowKey="id"
        loading={isLoading}
        columns={columns}
        dataSource={data || []}
        scroll={{ x: 'max-content' }}
        locale={{
          emptyText: (
            <span>
              No bookings yet. <Link to="/events">Browse events</Link>
            </span>
          ),
        }}
      />
    </div>
  );
}
