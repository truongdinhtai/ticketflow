import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Card, Descriptions, InputNumber, Button, Typography, Alert, Spin, Tag, Space, App,
} from 'antd';
import { useParams, useNavigate, Link } from 'react-router-dom';
import dayjs from 'dayjs';
import { fetchEvent } from '../api/events';
import { createBooking } from '../api/bookings';
import { errorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';

export default function EventDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { message } = App.useApp();
  const [quantity, setQuantity] = useState(1);

  const { data: event, isLoading, isError, error } = useQuery({
    queryKey: ['event', id],
    queryFn: () => fetchEvent(id),
  });

  const booking = useMutation({
    mutationFn: () =>
      createBooking({
        eventId: Number(id),
        customerName: user.displayName,
        quantity,
      }),
    onSuccess: (data) => {
      message.success(`Booking confirmed: ${data.bookingReference}`);
      queryClient.invalidateQueries({ queryKey: ['event', id] });
      queryClient.invalidateQueries({ queryKey: ['events'] });
      queryClient.invalidateQueries({ queryKey: ['my-bookings'] });
      navigate('/my-bookings');
    },
    onError: (e) => message.error(errorMessage(e, 'Booking failed')),
  });

  if (isLoading) {
    return <div style={{ textAlign: 'center', padding: 48 }}><Spin size="large" /></div>;
  }
  if (isError) {
    return <Alert type="error" showIcon message={errorMessage(error, 'Failed to load event')} />;
  }

  const soldOut = event.availableTickets <= 0;
  const maxQty = Math.min(20, event.availableTickets || 1);

  return (
    <div>
      <Link to="/events">← Back to events</Link>
      <Card style={{ marginTop: 12 }}>
        <Space align="center" style={{ justifyContent: 'space-between', width: '100%' }}>
          <Typography.Title level={3} style={{ margin: 0 }}>{event.name}</Typography.Title>
          <Tag color={soldOut ? 'red' : 'green'}>
            {soldOut ? 'Sold out' : `${event.availableTickets} tickets left`}
          </Tag>
        </Space>
        <Typography.Paragraph type="secondary" style={{ marginTop: 8 }}>
          {event.description}
        </Typography.Paragraph>

        <Descriptions column={1} bordered size="small" style={{ marginTop: 8 }}>
          <Descriptions.Item label="Venue">{event.venue}</Descriptions.Item>
          <Descriptions.Item label="City">{event.city}</Descriptions.Item>
          <Descriptions.Item label="Date">
            {dayjs(event.eventDateTime).format('dddd, DD MMM YYYY, HH:mm')}
          </Descriptions.Item>
          <Descriptions.Item label="Price">£{Number(event.ticketPrice).toFixed(2)} / ticket</Descriptions.Item>
          <Descriptions.Item label="Status">{event.status}</Descriptions.Item>
        </Descriptions>

        <Space style={{ marginTop: 16 }} wrap>
          <span>Quantity:</span>
          <InputNumber
            min={1}
            max={maxQty}
            value={quantity}
            onChange={(v) => setQuantity(v || 1)}
            disabled={soldOut}
          />
          <Button
            type="primary"
            loading={booking.isPending}
            disabled={soldOut}
            onClick={() => booking.mutate()}
          >
            Book {quantity} ticket(s) — £{(Number(event.ticketPrice) * quantity).toFixed(2)}
          </Button>
        </Space>
      </Card>
    </div>
  );
}
