import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { contactsAPI } from '../services/api';

function ContactForm({ formData, formError, formLoading, onSubmit, onClose,
                         onFormChange, onEmailChange, onPhoneChange, onAddEmail, onRemoveEmail,
                         onAddPhone, onRemovePhone, title, submitLabel }) {

    return (
        <div style={styles.overlay}>
            <div style={styles.modal}>
                <div style={styles.modalHeader}>
                    <h2 style={styles.modalTitle}>{title}</h2>
                    <button onClick={onClose} style={styles.closeBtn}>✕</button>
                </div>

                {formError && <div style={styles.error}>{formError}</div>}

                <form onSubmit={onSubmit}>
                    <div style={styles.row}>
                        <div style={styles.formGroup}>
                            <label style={styles.label}>First Name *</label>
                            <input name="firstName" value={formData.firstName}
                                   onChange={onFormChange} style={styles.input}
                                   placeholder="First name" required />
                        </div>
                        <div style={styles.formGroup}>
                            <label style={styles.label}>Last Name *</label>
                            <input name="lastName" value={formData.lastName}
                                   onChange={onFormChange} style={styles.input}
                                   placeholder="Last name" required />
                        </div>
                    </div>

                    <div style={styles.formGroup}>
                        <label style={styles.label}>Title</label>
                        <input name="title" value={formData.title}
                               onChange={onFormChange} style={styles.input}
                               placeholder="e.g. Mr, Dr, Prof" />
                    </div>

                    <div style={styles.sectionLabel}>Email Addresses</div>
                    {formData.emails.map((email, index) => (
                        <div key={index} style={styles.multiRow}>
                            <input value={email.email}
                                   onChange={(e) => onEmailChange(index, 'email', e.target.value)}
                                   style={{ ...styles.input, flex: 2 }} placeholder="Email address" type="email" />
                            <select value={email.label}
                                    onChange={(e) => onEmailChange(index, 'label', e.target.value)}
                                    style={{ ...styles.input, flex: 1 }}>
                                <option value="work">Work</option>
                                <option value="personal">Personal</option>
                                <option value="other">Other</option>
                            </select>
                            {formData.emails.length > 1 && (
                                <button type="button" onClick={() => onRemoveEmail(index)} style={styles.removeBtn}>✕</button>
                            )}
                        </div>
                    ))}
                    <button type="button" onClick={onAddEmail} style={styles.addBtn}>+ Add Email</button>

                    <div style={styles.sectionLabel}>Phone Numbers</div>
                    {formData.phones.map((phone, index) => (
                        <div key={index} style={styles.multiRow}>
                            <input value={phone.phoneNumber}
                                   onChange={(e) => onPhoneChange(index, 'phoneNumber', e.target.value)}
                                   style={{ ...styles.input, flex: 2 }} placeholder="Phone number" />
                            <select value={phone.label}
                                    onChange={(e) => onPhoneChange(index, 'label', e.target.value)}
                                    style={{ ...styles.input, flex: 1 }}>
                                <option value="mobile">Mobile</option>
                                <option value="work">Work</option>
                                <option value="home">Home</option>
                                <option value="personal">Personal</option>
                                <option value="other">Other</option>
                            </select>
                            {formData.phones.length > 1 && (
                                <button type="button" onClick={() => onRemovePhone(index)} style={styles.removeBtn}>✕</button>
                            )}
                        </div>
                    ))}
                    <button type="button" onClick={onAddPhone} style={styles.addBtn}>+ Add Phone</button>

                    <div style={styles.modalFooter}>
                        <button type="button" onClick={onClose} style={styles.cancelBtn}>Cancel</button>
                        <button type="submit" style={styles.submitBtn} disabled={formLoading}>
                            {formLoading ? 'Saving...' : submitLabel}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

function Contacts() {
    const navigate = useNavigate();
    const user = JSON.parse(localStorage.getItem('user') || '{}');

    const [contacts, setContacts] = useState([]);
    const [totalPages, setTotalPages] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const [search, setSearch] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const emptyForm = {
        firstName: '', lastName: '', title: '',
        emails: [{ email: '', label: 'work' }],
        phones: [{ phoneNumber: '', label: 'mobile' }],
    };

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [selectedContact, setSelectedContact] = useState(null);
    const [formData, setFormData] = useState(emptyForm);
    const [formError, setFormError] = useState('');
    const [formLoading, setFormLoading] = useState(false);

    const fetchContacts = useCallback(async () => {
        setLoading(true);
        try {
            const response = await contactsAPI.getContacts(currentPage, 10, search);
            setContacts(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (err) {
            setError('Failed to load contacts.');
        } finally {
            setLoading(false);
        }
    }, [currentPage, search]);

    useEffect(() => {
        fetchContacts();
    }, [fetchContacts]);

    const handleSearchChange = (e) => {
        setSearch(e.target.value);
        setCurrentPage(0);
    };

    const handleFormChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleEmailChange = (index, field, value) => {
        const updated = [...formData.emails];
        updated[index][field] = value;
        setFormData({ ...formData, emails: updated });
    };

    const handlePhoneChange = (index, field, value) => {
        const updated = [...formData.phones];
        updated[index][field] = value;
        setFormData({ ...formData, phones: updated });
    };

    const addEmail = () => {
        setFormData({ ...formData, emails: [...formData.emails, { email: '', label: 'personal' }] });
    };

    const removeEmail = (index) => {
        setFormData({ ...formData, emails: formData.emails.filter((_, i) => i !== index) });
    };

    const addPhone = () => {
        setFormData({ ...formData, phones: [...formData.phones, { phoneNumber: '', label: 'home' }] });
    };

    const removePhone = (index) => {
        setFormData({ ...formData, phones: formData.phones.filter((_, i) => i !== index) });
    };

    const handleCreate = async (e) => {
        e.preventDefault();
        setFormError('');
        setFormLoading(true);
        try {
            await contactsAPI.createContact(formData);
            setShowCreateModal(false);
            setFormData(emptyForm);
            fetchContacts();
        } catch (err) {
            setFormError(err.response?.data?.error || 'Failed to create contact!');
        } finally {
            setFormLoading(false);
        }
    };

    const openEditModal = (contact) => {
        setSelectedContact(contact);
        setFormData({
            firstName: contact.firstName,
            lastName: contact.lastName,
            title: contact.title || '',
            emails: contact.emails.length > 0 ? contact.emails : [{ email: '', label: 'work' }],
            phones: contact.phones.length > 0 ? contact.phones : [{ phoneNumber: '', label: 'mobile' }],
        });
        setShowEditModal(true);
    };

    const handleEdit = async (e) => {
        e.preventDefault();
        setFormError('');
        setFormLoading(true);
        try {
            await contactsAPI.updateContact(selectedContact.id, formData);
            setShowEditModal(false);
            fetchContacts();
        } catch (err) {
            setFormError(err.response?.data?.error || 'Failed to update contact!');
        } finally {
            setFormLoading(false);
        }
    };

    const openDeleteModal = (contact) => {
        setSelectedContact(contact);
        setShowDeleteModal(true);
    };

    const handleDelete = async () => {
        setFormLoading(true);
        try {
            await contactsAPI.deleteContact(selectedContact.id);
            setShowDeleteModal(false);
            fetchContacts();
        } catch (err) {
            setError('Failed to delete contact!');
        } finally {
            setFormLoading(false);
        }
    };

    const closeModals = () => {
        setShowCreateModal(false);
        setShowEditModal(false);
        setShowDeleteModal(false);
        setFormData(emptyForm);
        setFormError('');
        setSelectedContact(null);
    };
    const handleExport = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/contacts/export', {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                }
            });
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'contacts.csv';
            a.click();
            window.URL.revokeObjectURL(url);
        } catch (err) {
            setError('Export failed');
        }
    };

    const handleImport = async (e) => {
        const file = e.target.files[0];
        if (!file) return;
        const formData = new FormData();
        formData.append('file', file);
        try {
            const response = await fetch('http://localhost:8080/api/contacts/import', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                },
                body: formData
            });
            const result = await response.json();
            alert(`Import complete! Imported: ${result.imported}, Skipped: ${result.skipped}`);
            fetchContacts();
        } catch (err) {
            setError('Import failed');
        }
        e.target.value = '';
    };

    return (
        <div style={styles.page}>
            <nav style={styles.navbar}>
                <span style={styles.navBrand}>Contact Manager</span>
                <div style={styles.navRight}>
                    <span style={styles.navUser}>{user.firstName} {user.lastName}</span>
                    <button onClick={() => navigate('/profile')} style={styles.navBtn}>Profile</button>
                    <button onClick={() => { localStorage.clear(); navigate('/login'); }} style={styles.navBtnDanger}>Logout</button>
                </div>
            </nav>

            <div style={styles.content}>
                <div style={styles.topBar}>
                    <h1 style={styles.pageTitle}>My Contacts</h1>
                    <div style={{ display: 'flex', gap: '10px' }}>
                        <button onClick={handleExport} style={styles.exportBtn}>
                            ↓ Export CSV
                        </button>
                        <label style={styles.importBtn}>
                            ↑ Import CSV
                            <input type="file" accept=".csv"
                                   onChange={handleImport} style={{ display: 'none' }} />
                        </label>
                        <button onClick={() => { setFormData(emptyForm); setShowCreateModal(true); }}
                                style={styles.createBtn}>
                            + New Contact
                        </button>
                    </div>
                </div>

                <div style={styles.searchBar}>
                    <input
                        type="text" value={search} onChange={handleSearchChange}
                        placeholder="Search by first or last name..."
                        style={styles.searchInput}
                    />
                </div>

                {error && <div style={styles.error}>{error}</div>}

                {loading ? (
                    <div style={styles.loading}>Loading contacts...</div>
                ) : contacts.length === 0 ? (
                    <div style={styles.empty}>
                        {search ? `No contacts found for "${search}"` : 'No contacts yet. Create your first one!'}
                    </div>
                ) : (
                    <div style={styles.contactsList}>
                        {contacts.map((contact) => (
                            <div key={contact.id} style={styles.contactCard}>
                                <div style={styles.contactAvatar}>
                                    {contact.firstName[0]}{contact.lastName[0]}
                                </div>
                                <div style={styles.contactInfo}>
                                    <div style={styles.contactName}>
                                        {contact.title && <span style={styles.contactTitle}>{contact.title} </span>}
                                        {contact.firstName} {contact.lastName}
                                    </div>
                                    {contact.emails.length > 0 && (
                                        <div style={styles.contactDetail}>
                                            {contact.emails[0].email}
                                            <span style={styles.labelBadge}>{contact.emails[0].label}</span>
                                        </div>
                                    )}
                                    {contact.phones.length > 0 && (
                                        <div style={styles.contactDetail}>
                                            {contact.phones[0].phoneNumber}
                                            <span style={styles.labelBadge}>{contact.phones[0].label}</span>
                                        </div>
                                    )}
                                </div>
                                <div style={styles.contactActions}>
                                    <button onClick={() => openEditModal(contact)} style={styles.editBtn}>Edit</button>
                                    <button onClick={() => openDeleteModal(contact)} style={styles.deleteBtn}>Delete</button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}

                {totalPages > 1 && (
                    <div style={styles.pagination}>
                        <button onClick={() => setCurrentPage(p => p - 1)}
                                disabled={currentPage === 0} style={styles.pageBtn}>← Prev</button>
                        <span style={styles.pageInfo}>Page {currentPage + 1} of {totalPages}</span>
                        <button onClick={() => setCurrentPage(p => p + 1)}
                                disabled={currentPage === totalPages - 1} style={styles.pageBtn}>Next →</button>
                    </div>
                )}
            </div>

            {showCreateModal && (
                <ContactForm
                    formData={formData} formError={formError} formLoading={formLoading}
                    onSubmit={handleCreate} onClose={closeModals} onFormChange={handleFormChange}
                    onEmailChange={handleEmailChange} onPhoneChange={handlePhoneChange}
                    onAddEmail={addEmail} onRemoveEmail={removeEmail}
                    onAddPhone={addPhone} onRemovePhone={removePhone}
                    title="Create New Contact" submitLabel="Create Contact"
                />
            )}
            {showEditModal && (
                <ContactForm
                    formData={formData} formError={formError} formLoading={formLoading}
                    onSubmit={handleEdit} onClose={closeModals} onFormChange={handleFormChange}
                    onEmailChange={handleEmailChange} onPhoneChange={handlePhoneChange}
                    onAddEmail={addEmail} onRemoveEmail={removeEmail}
                    onAddPhone={addPhone} onRemovePhone={removePhone}
                    title="Update Contact" submitLabel="Save Changes"
                />
            )}

            {showDeleteModal && (
                <div style={styles.overlay}>
                    <div style={{ ...styles.modal, maxWidth: '400px' }}>
                        <div style={styles.modalHeader}>
                            <h2 style={styles.modalTitle}>Delete Contact</h2>
                            <button onClick={closeModals} style={styles.closeBtn}>✕</button>
                        </div>
                        <p style={{ color: '#555', margin: '16px 0' }}>
                            Are you sure you want to delete <strong>{selectedContact?.firstName} {selectedContact?.lastName}</strong>? This action cannot be undone.
                        </p>
                        <div style={styles.modalFooter}>
                            <button onClick={closeModals} style={styles.cancelBtn}>Cancel</button>
                            <button onClick={handleDelete} style={{ ...styles.submitBtn, backgroundColor: '#d93025' }} disabled={formLoading}>
                                {formLoading ? 'Deleting...' : 'Delete Contact'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

const styles = {
    page: { minHeight: '100vh', backgroundColor: '#f0f2f5' },
    navbar: {
        backgroundColor: '#092717', color: 'white',
        padding: '0 24px', height: '60px',
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        boxShadow: '0 2px 8px rgba(0,0,0,0.2)',
    },
    navBrand: { fontSize: '18px', fontWeight: 'bold' },
    navRight: { display: 'flex', alignItems: 'center', gap: '12px' },
    navUser: { fontSize: '14px', opacity: 0.9 },
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
    content: { maxWidth: '900px', margin: '0 auto', padding: '24px 16px' },
    topBar: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' },
    pageTitle: { fontSize: '24px', color: '#333', margin: 0 },
    createBtn: {
        padding: '10px 20px', backgroundColor: '#26c370',
        color: 'white', border: 'none', borderRadius: '8px',
        cursor: 'pointer', fontSize: '14px', fontWeight: '500',
    },
    searchBar: { marginBottom: '20px' },
    searchInput: {
        width: '100%', padding: '12px 16px',
        border: '1px solid #ddd', borderRadius: '8px',
        fontSize: '14px', boxSizing: 'border-box',
        boxShadow: '0 1px 4px rgba(0,0,0,0.05)',
    },
    error: {
        backgroundColor: '#fce8e6', color: '#c5221f',
        padding: '12px', borderRadius: '6px', marginBottom: '16px', fontSize: '14px',
    },
    loading: { textAlign: 'center', padding: '40px', color: '#666' },
    empty: {
        textAlign: 'center', padding: '60px', color: '#999',
        backgroundColor: 'white', borderRadius: '12px',
        boxShadow: '0 1px 4px rgba(0,0,0,0.08)',
    },
    contactsList: { display: 'flex', flexDirection: 'column', gap: '10px' },
    contactCard: {
        backgroundColor: 'white', borderRadius: '10px',
        padding: '16px', display: 'flex', alignItems: 'center', gap: '16px',
        boxShadow: '0 1px 4px rgba(0,0,0,0.08)',
    },
    contactAvatar: {
        width: '48px', height: '48px', borderRadius: '50%',
        backgroundColor: '#092717', color: 'white',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontSize: '16px', fontWeight: 'bold', flexShrink: 0,
    },
    contactInfo: { flex: 1 },
    contactName: { fontSize: '16px', fontWeight: '600', color: '#333', marginBottom: '4px' },
    contactTitle: { color: '#666', fontWeight: 'normal' },
    contactDetail: { fontSize: '13px', color: '#666', marginBottom: '2px' },
    labelBadge: {
        marginLeft: '6px', padding: '1px 6px',
        backgroundColor: '#e8f0fe', color: '#092717',
        borderRadius: '10px', fontSize: '11px',
    },
    contactActions: { display: 'flex', gap: '8px' },
    editBtn: {
        padding: '6px 14px', backgroundColor: '#e8f0fe',
        color: '#092717', border: 'none', borderRadius: '6px', cursor: 'pointer', fontSize: '13px',
    },
    deleteBtn: {
        padding: '6px 14px', backgroundColor: '#fce8e6',
        color: '#d93025', border: 'none', borderRadius: '6px', cursor: 'pointer', fontSize: '13px',
    },
    pagination: {
        display: 'flex', justifyContent: 'center', alignItems: 'center',
        gap: '16px', marginTop: '24px',
    },
    pageBtn: {
        padding: '8px 16px', backgroundColor: 'white',
        border: '1px solid #ddd', borderRadius: '6px', cursor: 'pointer',
    },
    pageInfo: { color: '#666', fontSize: '14px' },
    overlay: {
        position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
        backgroundColor: 'rgba(0,0,0,0.5)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        zIndex: 1000,
    },
    modal: {
        backgroundColor: 'white', borderRadius: '12px',
        padding: '24px', width: '90%', maxWidth: '560px',
        maxHeight: '85vh', overflowY: 'auto',
        boxShadow: '0 8px 32px rgba(0,0,0,0.2)',
    },
    modalHeader: {
        display: 'flex', justifyContent: 'space-between',
        alignItems: 'center', marginBottom: '20px',
    },
    modalTitle: { fontSize: '18px', color: '#333', margin: 0 },
    closeBtn: {
        background: 'none', border: 'none', fontSize: '18px',
        cursor: 'pointer', color: '#666', padding: '4px',
    },
    row: { display: 'flex', gap: '12px' },
    formGroup: { marginBottom: '14px', flex: 1 },
    label: { display: 'block', marginBottom: '5px', color: '#555', fontSize: '13px', fontWeight: '500' },
    input: {
        width: '100%', padding: '9px 12px',
        border: '1px solid #ddd', borderRadius: '6px',
        fontSize: '14px', boxSizing: 'border-box',
    },
    sectionLabel: {
        fontSize: '13px', fontWeight: '600', color: '#333',
        margin: '16px 0 8px', borderBottom: '1px solid #eee', paddingBottom: '4px',
    },
    multiRow: { display: 'flex', gap: '8px', alignItems: 'center', marginBottom: '8px' },
    removeBtn: {
        background: '#fce8e6', color: '#d93025', border: 'none',
        borderRadius: '4px', cursor: 'pointer', padding: '6px 10px', flexShrink: 0,
    },
    addBtn: {
        background: 'none', border: '1px dashed #092717', color: '#092717',
        borderRadius: '6px', cursor: 'pointer', padding: '6px 12px',
        fontSize: '13px', marginBottom: '8px',
    },
    modalFooter: { display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '20px' },
    cancelBtn: {
        padding: '9px 20px', backgroundColor: 'white',
        border: '1px solid #ddd', borderRadius: '6px', cursor: 'pointer',
    },
    submitBtn: {
        padding: '9px 20px', backgroundColor: '#092717',
        color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer',
    },
    exportBtn: {
        padding: '10px 16px', backgroundColor: 'white',
        color: '#092717', border: '1px solid #092717',
        borderRadius: '8px', cursor: 'pointer', fontSize: '14px',
    },
    importBtn: {
        padding: '10px 16px', backgroundColor: 'white',
        color: '#092717', border: '1px solid #092717',
        borderRadius: '8px', cursor: 'pointer', fontSize: '14px',
        display: 'inline-flex', alignItems: 'center',
    },
};

export default Contacts;