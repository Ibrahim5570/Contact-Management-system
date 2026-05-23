import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { userAPI, authAPI } from '../services/api';

function Profile() {
    const navigate = useNavigate();
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // Change password modal
    const [showPasswordModal, setShowPasswordModal] = useState(false);
    const [passwordForm, setPasswordForm] = useState({
        currentPassword: '', newPassword: '', confirmPassword: ''
    });
    const [passwordError, setPasswordError] = useState('');
    const [passwordSuccess, setPasswordSuccess] = useState('');
    const [passwordLoading, setPasswordLoading] = useState(false);

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const response = await userAPI.getProfile();
                setProfile(response.data);
            } catch (err) {
                setError('Failed to load profile');
            } finally {
                setLoading(false);
            }
        };
        fetchProfile();
    }, []);

    const handlePasswordChange = (e) => {
        setPasswordForm({ ...passwordForm, [e.target.name]: e.target.value });
    };

    const handlePasswordSubmit = async (e) => {
        e.preventDefault();
        setPasswordError('');
        setPasswordSuccess('');

        if (passwordForm.newPassword !== passwordForm.confirmPassword) {
            setPasswordError('New passwords do not match');
            return;
        }

        if (passwordForm.newPassword.length < 6) {
            setPasswordError('Password must be at least 6 characters');
            return;
        }

        setPasswordLoading(true);
        try {
            await authAPI.changePassword({
                currentPassword: passwordForm.currentPassword,
                newPassword: passwordForm.newPassword,
            });
            setPasswordSuccess('Password changed successfully!');
            setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
            setTimeout(() => {
                setShowPasswordModal(false);
                setPasswordSuccess('');
            }, 2000);
        } catch (err) {
            setPasswordError(err.response?.data?.error || 'Failed to change password');
        } finally {
            setPasswordLoading(false);
        }
    };

    const handleLogout = () => {
        localStorage.clear();
        navigate('/login');
    };

    if (loading) return (
        <div style={styles.loadingPage}>Loading profile...</div>
    );

    return (
        <div style={styles.page}>
            {/* Navbar */}
            <nav style={styles.navbar}>
                <span style={styles.navBrand}>Contact Manager</span>
                <div style={styles.navRight}>
                    <button onClick={() => navigate('/contacts')} style={styles.navBtn}>
                        ← My Contacts
                    </button>
                    <button onClick={handleLogout} style={styles.navBtnDanger}>Logout</button>
                </div>
            </nav>

            <div style={styles.content}>
                <h1 style={styles.pageTitle}>User Profile</h1>

                {error && <div style={styles.error}>{error}</div>}

                {profile && (
                    <div style={styles.profileCard}>
                        {/* Avatar */}
                        <div style={styles.avatarSection}>
                            <div style={styles.avatar}>
                                {profile.firstName?.[0]}{profile.lastName?.[0]}
                            </div>
                            <h2 style={styles.profileName}>
                                {profile.firstName} {profile.lastName}
                            </h2>
                            <p style={styles.profileEmail}>{profile.email}</p>
                        </div>

                        {/* Details */}
                        <div style={styles.detailsSection}>
                            <div style={styles.detailRow}>
                                <span style={styles.detailLabel}>First Name</span>
                                <span style={styles.detailValue}>{profile.firstName}</span>
                            </div>
                            <div style={styles.detailRow}>
                                <span style={styles.detailLabel}>Last Name</span>
                                <span style={styles.detailValue}>{profile.lastName}</span>
                            </div>
                            <div style={styles.detailRow}>
                                <span style={styles.detailLabel}>Email</span>
                                <span style={styles.detailValue}>{profile.email}</span>
                            </div>
                            {profile.phoneNumber && (
                                <div style={styles.detailRow}>
                                    <span style={styles.detailLabel}>Phone</span>
                                    <span style={styles.detailValue}>{profile.phoneNumber}</span>
                                </div>
                            )}
                        </div>

                        {/* Actions */}
                        <div style={styles.actionsSection}>
                            <button
                                onClick={() => setShowPasswordModal(true)}
                                style={styles.changePasswordBtn}>
                                Change Password
                            </button>
                            <button onClick={handleLogout} style={styles.logoutBtn}>
                                Logout
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* Change Password Modal */}
            {showPasswordModal && (
                <div style={styles.overlay}>
                    <div style={styles.modal}>
                        <div style={styles.modalHeader}>
                            <h2 style={styles.modalTitle}>Change Password</h2>
                            <button onClick={() => {
                                setShowPasswordModal(false);
                                setPasswordError('');
                                setPasswordSuccess('');
                                setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
                            }} style={styles.closeBtn}>✕</button>
                        </div>

                        {passwordError && <div style={styles.error}>{passwordError}</div>}
                        {passwordSuccess && <div style={styles.success}>{passwordSuccess}</div>}

                        <form onSubmit={handlePasswordSubmit}>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>Current Password</label>
                                <input
                                    type="password" name="currentPassword"
                                    value={passwordForm.currentPassword}
                                    onChange={handlePasswordChange}
                                    style={styles.input} placeholder="Enter current password" required />
                            </div>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>New Password</label>
                                <input
                                    type="password" name="newPassword"
                                    value={passwordForm.newPassword}
                                    onChange={handlePasswordChange}
                                    style={styles.input} placeholder="Minimum 6 characters" required />
                            </div>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>Confirm New Password</label>
                                <input
                                    type="password" name="confirmPassword"
                                    value={passwordForm.confirmPassword}
                                    onChange={handlePasswordChange}
                                    style={styles.input} placeholder="Repeat new password" required />
                            </div>
                            <div style={styles.modalFooter}>
                                <button type="button" onClick={() => setShowPasswordModal(false)}
                                        style={styles.cancelBtn}>Cancel</button>
                                <button type="submit" style={styles.submitBtn} disabled={passwordLoading}>
                                    {passwordLoading ? 'Saving...' : 'Change Password'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}

const styles = {
    page: { minHeight: '100vh', backgroundColor: '#f0f2f5' },
    loadingPage: {
        minHeight: '100vh', display: 'flex',
        alignItems: 'center', justifyContent: 'center', color: '#666',
    },
    navbar: {
        backgroundColor: '#092717', color: 'white',
        padding: '0 24px', height: '60px',
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        boxShadow: '0 2px 8px rgba(0,0,0,0.2)',
    },
    navBrand: { fontSize: '18px', fontWeight: 'bold' },
    navRight: { display: 'flex', alignItems: 'center', gap: '12px' },
    navBtn: {
        padding: '6px 14px', backgroundColor: 'rgba(255,255,255,0.2)',
        color: 'white', border: '1px solid rgba(255,255,255,0.4)',
        borderRadius: '6px', cursor: 'pointer', fontSize: '13px',
    },
    navBtnDanger: {
        padding: '6px 14px', backgroundColor: '#d93025',
        color: 'white', border: 'none',
        borderRadius: '6px', cursor: 'pointer', fontSize: '13px',
    },
    content: { maxWidth: '600px', margin: '40px auto', padding: '0 16px' },
    pageTitle: { fontSize: '24px', color: '#333', marginBottom: '24px' },
    error: {
        backgroundColor: '#fce8e6', color: '#c5221f',
        padding: '12px', borderRadius: '6px', marginBottom: '16px', fontSize: '14px',
    },
    success: {
        backgroundColor: '#e6f4ea', color: '#137333',
        padding: '12px', borderRadius: '6px', marginBottom: '16px', fontSize: '14px',
    },
    profileCard: {
        backgroundColor: 'white', borderRadius: '12px',
        boxShadow: '0 2px 12px rgba(0,0,0,0.08)', overflow: 'hidden',
    },
    avatarSection: {
        backgroundColor: '#092717', padding: '32px',
        display: 'flex', flexDirection: 'column', alignItems: 'center',
    },
    avatar: {
        width: '80px', height: '80px', borderRadius: '50%',
        backgroundColor: 'rgba(255,255,255,0.2)', color: 'white',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontSize: '28px', fontWeight: 'bold', marginBottom: '12px',
    },
    profileName: { color: 'white', fontSize: '22px', margin: '0 0 4px' },
    profileEmail: { color: 'rgba(255,255,255,0.8)', fontSize: '14px', margin: 0 },
    detailsSection: { padding: '24px' },
    detailRow: {
        display: 'flex', justifyContent: 'space-between',
        alignItems: 'center', padding: '12px 0',
        borderBottom: '1px solid #f0f0f0',
    },
    detailLabel: { fontSize: '14px', color: '#666', fontWeight: '500' },
    detailValue: { fontSize: '14px', color: '#333' },
    actionsSection: {
        padding: '24px', display: 'flex', gap: '12px',
        borderTop: '1px solid #f0f0f0',
    },
    changePasswordBtn: {
        flex: 1, padding: '10px', backgroundColor: '#092717',
        color: 'white', border: 'none', borderRadius: '8px',
        cursor: 'pointer', fontSize: '14px',
    },
    logoutBtn: {
        flex: 1, padding: '10px', backgroundColor: 'white',
        color: '#d93025', border: '1px solid #d93025', borderRadius: '8px',
        cursor: 'pointer', fontSize: '14px',
    },
    overlay: {
        position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
        backgroundColor: 'rgba(0,0,0,0.5)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        zIndex: 1000,
    },
    modal: {
        backgroundColor: 'white', borderRadius: '12px',
        padding: '24px', width: '90%', maxWidth: '440px',
        boxShadow: '0 8px 32px rgba(0,0,0,0.2)',
    },
    modalHeader: {
        display: 'flex', justifyContent: 'space-between',
        alignItems: 'center', marginBottom: '20px',
    },
    modalTitle: { fontSize: '18px', color: '#333', margin: 0 },
    closeBtn: {
        background: 'none', border: 'none', fontSize: '18px',
        cursor: 'pointer', color: '#666',
    },
    formGroup: { marginBottom: '16px' },
    label: {
        display: 'block', marginBottom: '6px',
        color: '#555', fontSize: '13px', fontWeight: '500',
    },
    input: {
        width: '100%', padding: '10px 12px',
        border: '1px solid #ddd', borderRadius: '6px',
        fontSize: '14px', boxSizing: 'border-box',
    },
    modalFooter: {
        display: 'flex', justifyContent: 'flex-end',
        gap: '10px', marginTop: '20px',
    },
    cancelBtn: {
        padding: '9px 20px', backgroundColor: 'white',
        border: '1px solid #ddd', borderRadius: '6px', cursor: 'pointer',
    },
    submitBtn: {
        padding: '9px 20px', backgroundColor: '#092717',
        color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer',
    },
};

export default Profile;