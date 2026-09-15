import { Form, Input, Button, Card, Typography, Alert } from 'antd';
import { useMutation } from '@tanstack/react-query';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { login } from '../api/auth';
import { errorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';

export default function LoginPage() {
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from || '/events';

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: (data) => {
      signIn(data);
      navigate(from, { replace: true });
    },
  });

  return (
    <div style={{ maxWidth: 380, margin: '48px auto', padding: 16 }}>
      <Card>
        <Typography.Title level={3} style={{ textAlign: 'center' }}>
          🎫 TicketFlow — Login
        </Typography.Title>
        {mutation.isError && (
          <Alert
            type="error"
            showIcon
            style={{ marginBottom: 16 }}
            message={errorMessage(mutation.error, 'Login failed')}
          />
        )}
        <Form layout="vertical" onFinish={(values) => mutation.mutate(values)}>
          <Form.Item
            label="Email"
            name="email"
            rules={[{ required: true, type: 'email', message: 'Enter a valid email' }]}
          >
            <Input placeholder="you@example.com" autoComplete="email" />
          </Form.Item>
          <Form.Item
            label="Password"
            name="password"
            rules={[{ required: true, message: 'Enter your password' }]}
          >
            <Input.Password placeholder="••••••" autoComplete="current-password" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block loading={mutation.isPending}>
            Login
          </Button>
        </Form>
        <Typography.Paragraph style={{ textAlign: 'center', marginTop: 16, marginBottom: 0 }}>
          No account? <Link to="/register">Register</Link>
        </Typography.Paragraph>
      </Card>
    </div>
  );
}
