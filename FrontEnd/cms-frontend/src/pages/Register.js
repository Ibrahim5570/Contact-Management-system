import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authAPI } from '../services/api';

function Register() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        firstName: '', lastName: '', email: '',
        phoneNumber: '', password: '', confirmPassword: ''
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        if (formData.password !== formData.confirmPassword) {
            setError('Passwords do not match');
            return;
        }
        setLoading(true);
        try {
            const response = await authAPI.register({
                firstName: formData.firstName,
                lastName: formData.lastName,
                email: formData.email,
                phoneNumber: formData.phoneNumber,
                password: formData.password,
            });
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('user', JSON.stringify({
                email: response.data.email,
                firstName: response.data.firstName,
                lastName: response.data.lastName,
            }));
            navigate('/contacts');
        } catch (err) {
            setError(err.response?.data?.error || 'Registration failed. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={styles.container}>
            <div style={styles.card}>
                <h1 style={styles.title}>Contact Manager</h1>
                <h2 style={styles.subtitle}>Create Account</h2>

                {error && <div style={styles.error}>{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div style={styles.row}>
                        <div style={styles.formGroup}>
                            <label style={styles.label}>First Name</label>
                            <input
                                type="text" name="firstName"
                                value={formData.firstName} onChange={handleChange}
                                style={styles.input} placeholder="First name" required
                            />
                        </div>
                        <div style={styles.formGroup}>
                            <label style={styles.label}>Last Name</label>
                            <input
                                type="text" name="lastName"
                                value={formData.lastName} onChange={handleChange}
                                style={styles.input} placeholder="Last name" required
                            />
                        </div>
                    </div>

                    <div style={styles.formGroup}>
                        <label style={styles.label}>Email</label>
                        <input
                            type="email" name="email"
                            value={formData.email} onChange={handleChange}
                            style={styles.input} placeholder="Enter your email" required
                        />
                    </div>

                    <div style={styles.formGroup}>
                        <label style={styles.label}>Phone Number (optional)</label>
                        <input
                            type="text" name="phoneNumber"
                            value={formData.phoneNumber} onChange={handleChange}
                            style={styles.input} placeholder="e.g. 03001234567"
                        />
                    </div>

                    <div style={styles.formGroup}>
                        <label style={styles.label}>Password</label>
                        <input
                            type="password" name="password"
                            value={formData.password} onChange={handleChange}
                            style={styles.input} placeholder="Minimum 6 characters" required
                        />
                    </div>

                    <div style={styles.formGroup}>
                        <label style={styles.label}>Confirm Password</label>
                        <input
                            type="password" name="confirmPassword"
                            value={formData.confirmPassword} onChange={handleChange}
                            style={styles.input} placeholder="Repeat password" required
                        />
                    </div>

                    <button type="submit" style={styles.button} disabled={loading}>
                        {loading ? 'Creating account...' : 'Create Account'}
                    </button>
                </form>

                <p style={styles.linkText}>
                    Already have an account?{' '}
                    <Link to="/login" style={styles.link}>Sign in here</Link>
                </p>
            </div>
        </div>
    );
}

const styles = {
    container: {
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#f0f2f5',
    },
    card: {
        backgroundColor: 'white',
        padding: '40px',
        borderRadius: '12px',
        boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
        width: '100%',
        maxWidth: '480px',
    },
    title: {
        textAlign: 'center',
        color: '#1a73e8',
        marginBottom: '8px',
        fontSize: '24px',
    },
    subtitle: {
        textAlign: 'center',
        color: '#333',
        marginBottom: '24px',
        fontSize: '18px',
        fontWeight: 'normal',
    },
    error: {
        backgroundColor: '#fce8e6',
        color: '#c5221f',
        padding: '12px',
        borderRadius: '6px',
        marginBottom: '16px',
        fontSize: '14px',
    },
    row: {
        display: 'flex',
        gap: '12px',
    },
    formGroup: {
        marginBottom: '16px',
        flex: 1,
    },
    label: {
        display: 'block',
        marginBottom: '6px',
        color: '#555',
        fontSize: '14px',
        fontWeight: '500',
    },
    input: {
        width: '100%',
        padding: '10px 12px',
        border: '1px solid #ddd',
        borderRadius: '6px',
        fontSize: '14px',
        boxSizing: 'border-box',
    },
    button: {
        width: '100%',
        padding: '12px',
        backgroundColor: '#1a73e8',
        color: 'white',
        border: 'none',
        borderRadius: '6px',
        fontSize: '16px',
        cursor: 'pointer',
        marginTop: '8px',
    },
    linkText: {
        textAlign: 'center',
        marginTop: '20px',
        color: '#666',
        fontSize: '14px',
    },
    link: {
        color: '#1a73e8',
        textDecoration: 'none',
    },
};

export default Register;