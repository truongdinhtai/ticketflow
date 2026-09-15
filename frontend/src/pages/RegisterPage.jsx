import { Form, Input, Button, Card, Typography, Alert } from 'antd';
import { useMutation } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router-dom';
import { register } from '../api/auth';
import { errorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';

export default function RegisterPage() {
  const { signIn } = useAuth();
  const navigate = useNavigate();

  const mutation = useMutation({
    mutationFn: register,
    onSuccess: (data) => {
      signIn(data);
      navigate('/events', { replace: true });
    },
  });

  return (
    <div style={{ maxWidth: 380, margin: '48px auto', padding: 16 }}>
      <Card>
        <Typography.Title level={3} style={{ textAlign: 'center' }}>
          🎫 Create your account
        </Typography.Title>
        {mutation.isError && (
          <Alert
            type="error"
            showIcon
            style={{ marginBottom: 16 }}
            message={errorMessage(mutation.error, 'Registration failed')}
          />
        )}
        <Form layout="vertical" onFinish={(values) => mutation.mutate(values)}>
          <Form.Item
            label="Name"
            name="displayName"
            rules={[{ required: true, message: 'Enter your name' }]}
          >
            <Input placeholder="Alice Nguyen" />
          </Form.Item>
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
            rules={[{ required: true, min: 6, message: 'At least 6 characters' }]}
          >
            <Input.Password placeholder="••••••" autoComplete="new-password" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block loading={mutation.isPending}>
            Register
          </Button>
        </Form>
        <Typography.Paragraph style={{ textAlign: 'center', marginTop: 16, marginBottom: 0 }}>
          Already have an account? <Link to="/login">Login</Link>
        </Typography.Paragraph>
      </Card>
    </div>
  );
}
