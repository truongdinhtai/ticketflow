import { useQuery } from '@tanstack/react-query';
import { Row, Col, Card, Typography, Tag, Button, Space, Spin } from 'antd';
import { SERVICES, fetchHealth } from '../api/health';

function statusColor(status) {
  if (status === 'UP') return 'green';
  if (status === 'DOWN' || status === 'OUT_OF_SERVICE') return 'red';
  return 'orange';
}

export default function HealthPage() {
  const { data, isLoading, refetch, isFetching, dataUpdatedAt } = useQuery({
    queryKey: ['health'],
    // Probe every service in parallel; each resolves to {key, status}.
    queryFn: () => Promise.all(SERVICES.map((s) => fetchHealth(s.key))),
    refetchInterval: 10000, // auto-refresh every 10s
  });

  const byKey = Object.fromEntries((data || []).map((r) => [r.key, r.status]));

  return (
    <div>
      <Space style={{ justifyContent: 'space-between', width: '100%', marginBottom: 12 }} wrap>
        <Typography.Title level={3} style={{ margin: 0 }}>Service Health</Typography.Title>
        <Space>
          {dataUpdatedAt > 0 && (
            <Typography.Text type="secondary">
              Updated {new Date(dataUpdatedAt).toLocaleTimeString()}
            </Typography.Text>
          )}
          <Button onClick={() => refetch()} loading={isFetching}>Refresh</Button>
        </Space>
      </Space>

      <Typography.Paragraph type="secondary">
        Each card polls that service's <code>/actuator/health</code> (via proxy) every 10s.
      </Typography.Paragraph>

      {isLoading ? (
        <div style={{ textAlign: 'center', padding: 48 }}><Spin size="large" /></div>
      ) : (
        <Row gutter={[16, 16]}>
          {SERVICES.map((s) => {
            const status = byKey[s.key] || 'UNKNOWN';
            return (
              <Col xs={24} sm={12} lg={8} key={s.key}>
                <Card size="small">
                  <Space style={{ justifyContent: 'space-between', width: '100%' }}>
                    <Typography.Text strong>{s.label}</Typography.Text>
                    <Tag color={statusColor(status)}>{status}</Tag>
                  </Space>
                </Card>
              </Col>
            );
          })}
        </Row>
      )}
    </div>
  );
}
