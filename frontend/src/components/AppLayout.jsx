import { Layout, Menu, Button, Space, Typography, Grid } from 'antd';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

const { Header, Content } = Layout;
const { useBreakpoint } = Grid;

const NAV = [
  { key: '/events', label: <Link to="/events">Events</Link> },
  { key: '/my-bookings', label: <Link to="/my-bookings">My Bookings</Link> },
  { key: '/health', label: <Link to="/health">Health</Link> },
];

export default function AppLayout() {
  const { user, signOut } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const screens = useBreakpoint();

  // Highlight the active top-level section.
  const selected = NAV.map((n) => n.key).filter((k) => location.pathname.startsWith(k));

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 16,
          padding: '0 16px',
          flexWrap: 'wrap',
        }}
      >
        <Typography.Title level={4} style={{ color: '#fff', margin: 0, whiteSpace: 'nowrap' }}>
          🎫 TicketFlow
        </Typography.Title>
        <Menu
          theme="dark"
          mode="horizontal"
          selectedKeys={selected}
          items={NAV}
          style={{ flex: 1, minWidth: 200 }}
        />
        <Space>
          {screens.sm && (
            <Typography.Text style={{ color: '#fff' }}>
              {user?.displayName || user?.email}
            </Typography.Text>
          )}
          <Button
            size="small"
            onClick={() => {
              signOut();
              navigate('/login');
            }}
          >
            Logout
          </Button>
        </Space>
      </Header>
      <Content style={{ padding: 16, maxWidth: 1100, width: '100%', margin: '0 auto' }}>
        <Outlet />
      </Content>
    </Layout>
  );
}
