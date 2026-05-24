import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authAPI } from '../services/api';
import { authStyles as styles } from '../styles/authStyles';

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
        if (!formData.email && !formData.phoneNumber) {
            setError('Please provide either an email or phone number');
            return;
        }
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
                        <label style={styles.label}>Email <span style={{color:'#999', fontWeight:'normal'}}>(or use phone below)</span></label>
                        <input
                            type="email" name="email"
                            value={formData.email} onChange={handleChange}
                            style={styles.input} placeholder="Enter your email"
                        />
                    </div>

                    <div style={styles.formGroup}>
                        <label style={styles.label}>Phone Number <span style={{color:'#999', fontWeight:'normal'}}>(or use email above)</span></label>
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

export default Register;