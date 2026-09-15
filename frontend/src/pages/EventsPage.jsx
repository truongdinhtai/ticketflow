import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Row, Col, Card, Select, DatePicker, Button, Typography, Alert, Spin, Empty, Tag, Space } from 'antd';
import { Link } from 'react-router-dom';
import dayjs from 'dayjs';
import { fetchEvents } from '../api/events';
import { errorMessage } from '../api/client';

export default function EventsPage() {
  const [city, setCity] = useState(null);
  const [date, setDate] = useState(null);

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['events'],
    queryFn: () => fetchEvents({ page: 0, size: 100 }),
  });

  const events = data?.content || [];

  const cities = useMemo(
    () => [...new Set(events.map((e) => e.city))].sort(),
    [events],
  );

  const filtered = events.filter((e) => {
    if (city && e.city !== city) return false;
    if (date && !dayjs(e.eventDateTime).isSame(date, 'day')) return false;
    return true;
  });

  return (
    <div>
      <Typography.Title level={3}>Events</Typography.Title>

      <Space wrap style={{ marginBottom: 16 }}>
        <Select
          allowClear
          placeholder="Filter by city"
          style={{ width: 200 }}
          value={city}
          onChange={setCity}
          options={cities.map((c) => ({ value: c, label: c }))}
        />
        <DatePicker placeholder="Filter by date" value={date} onChange={setDate} />
        {(city || date) && (
          <Button
            onClick={() => {
              setCity(null);
              setDate(null);
            }}
          >
            Clear filters
          </Button>
        )}
      </Space>

      {isLoading && (
        <div style={{ textAlign: 'center', padding: 48 }}>
          <Spin size="large" />
        </div>
      )}

      {isError && <Alert type="error" showIcon message={errorMessage(error, 'Failed to load events')} />}

      {!isLoading && !isError && filtered.length === 0 && <Empty description="No events found" />}

      <Row gutter={[16, 16]}>
        {filtered.map((e) => (
          <Col xs={24} sm={12} lg={8} key={e.id}>
            <Card
              title={e.name}
              extra={<Tag color={e.availableTickets > 0 ? 'green' : 'red'}>
                {e.availableTickets > 0 ? `${e.availableTickets} left` : 'Sold out'}
              </Tag>}
            >
              <p style={{ margin: '4px 0' }}>📍 {e.venue}, {e.city}</p>
              <p style={{ margin: '4px 0' }}>🗓️ {dayjs(e.eventDateTime).format('DD MMM YYYY, HH:mm')}</p>
              <p style={{ margin: '4px 0' }}>💷 {Number(e.ticketPrice).toFixed(2)}</p>
              <Link to={`/events/${e.id}`}>
                <Button type="primary" block style={{ marginTop: 8 }}>
                  View & book
                </Button>
              </Link>
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
}
