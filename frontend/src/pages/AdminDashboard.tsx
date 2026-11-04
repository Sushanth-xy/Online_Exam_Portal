
import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import {
  createAdminUser,
  getAdminUsers,
  getAuditLogs,
  getProctoringIncidents,
  updateAdminUserRole,
  updateAdminUserStatus,
  type AdminUser,
  type AuditLog,
  type ProctoringIncident,
} from '../api/admin';
import './AdminDashboard.css';

type DashboardTab = 'users' | 'audit' | 'proctoring';
type UserRole = 'STUDENT' | 'INSTRUCTOR' | 'ADMIN';

interface AuditPage {
  content: AuditLog[];
  number: number;
  totalPages: number;
  totalElements: number;
}

function getErrorMessage(error: unknown): string {
  if (
    typeof error === 'object' &&
    error !== null &&
    'response' in error
  ) {
    const response = error.response;

    if (
      typeof response === 'object' &&
      response !== null &&
      'data' in response
    ) {
      const data = response.data;

      if (
        typeof data === 'object' &&
        data !== null &&
        'message' in data &&
        typeof data.message === 'string'
      ) {
        return data.message;
      }
    }
  }

  return error instanceof Error
    ? error.message
    : 'Something went wrong. Please try again.';
}

export default function AdminDashboard() {
  const { user, token, logout } = useAuth();

  const [activeTab, setActiveTab] = useState<DashboardTab>('users');

  const [users, setUsers] = useState<AdminUser[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [incidents, setIncidents] = useState<ProctoringIncident[]>([]);

  const [auditPage, setAuditPage] = useState(0);
  const [auditTotalPages, setAuditTotalPages] = useState(0);

  const [usersLoading, setUsersLoading] = useState(false);
  const [auditLoading, setAuditLoading] = useState(false);
  const [incidentsLoading, setIncidentsLoading] = useState(false);
  const [creatingUser, setCreatingUser] = useState(false);

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [newName, setNewName] = useState('');
  const [newEmail, setNewEmail] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [newRole, setNewRole] = useState<UserRole>('STUDENT');

  const loadUsers = useCallback(async () => {
    if (!token) return;

    setUsersLoading(true);
    setError('');

    try {
      const data = await getAdminUsers(token);
      setUsers(data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setUsersLoading(false);
    }
  }, [token]);

  const loadAuditLogs = useCallback(async (page = 0) => {
    if (!token) return;

    setAuditLoading(true);
    setError('');

    try {
      const result = (await getAuditLogs(
        token,
        page,
        20
      )) as AuditPage;

      setAuditLogs(result.content ?? []);
      setAuditPage(result.number ?? page);
      setAuditTotalPages(result.totalPages ?? 0);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setAuditLoading(false);
    }
  }, [token]);

  const loadIncidents = useCallback(async () => {
    if (!token) return;

    setIncidentsLoading(true);
    setError('');

    try {
      const data = await getProctoringIncidents(token);
      setIncidents(data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIncidentsLoading(false);
    }
  }, [token]);

  useEffect(() => {
    void loadUsers();
  }, [loadUsers]);

  useEffect(() => {
    if (activeTab === 'audit') {
      void loadAuditLogs(0);
    } else if (activeTab === 'proctoring') {
      void loadIncidents();
    }
  }, [activeTab, loadAuditLogs, loadIncidents]);

  const handleCreateUser = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!token) return;

    setCreatingUser(true);
    setError('');
    setSuccess('');

    try {
      await createAdminUser(token, {
        name: newName.trim(),
        email: newEmail.trim(),
        password: newPassword,
        role: newRole,
      });

      setNewName('');
      setNewEmail('');
      setNewPassword('');
      setNewRole('STUDENT');

      setSuccess('User created successfully.');
      await loadUsers();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setCreatingUser(false);
    }
  };

  const handleRoleChange = async (
    targetUser: AdminUser,
    role: UserRole
  ) => {
    if (!token || targetUser.role === role) return;

    setError('');
    setSuccess('');

    try {
      await updateAdminUserRole(token, targetUser.id, role);
      setSuccess(`Role updated for ${targetUser.email}.`);
      await loadUsers();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const handleStatusChange = async (targetUser: AdminUser) => {
    if (!token) return;

    const nextStatus = !targetUser.active;

    if (
      targetUser.email.toLowerCase() === user?.email.toLowerCase() &&
      !nextStatus
    ) {
      setError('You cannot deactivate your own account.');
      return;
    }

    const confirmed = window.confirm(
      `${nextStatus ? 'Activate' : 'Deactivate'} ${targetUser.email}?`
    );

    if (!confirmed) return;

    setError('');
    setSuccess('');

    try {
      await updateAdminUserStatus(
        token,
        targetUser.id,
        nextStatus
      );

      setSuccess(
        `${targetUser.email} has been ${
          nextStatus ? 'activated' : 'deactivated'
        }.`
      );

      await loadUsers();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const renderUsersTab = () => (
    <section className="admin-section">
      <div className="section-heading">
        <div>
          <h2>User Management</h2>
          <p>Create accounts and manage user roles and access.</p>
        </div>

        <button
          type="button"
          onClick={() => void loadUsers()}
          disabled={usersLoading}
        >
          {usersLoading ? 'Refreshing...' : 'Refresh Users'}
        </button>
      </div>

      <form className="admin-form" onSubmit={handleCreateUser}>
        <h3>Create User</h3>

        <div className="form-grid">
          <label>
            Full Name
            <input
              type="text"
              value={newName}
              onChange={(event) => setNewName(event.target.value)}
              maxLength={100}
              required
            />
          </label>

          <label>
            Email
            <input
              type="email"
              value={newEmail}
              onChange={(event) => setNewEmail(event.target.value)}
              maxLength={255}
              required
            />
          </label>

          <label>
            Temporary Password
            <input
              type="password"
              value={newPassword}
              onChange={(event) => setNewPassword(event.target.value)}
              minLength={8}
              maxLength={72}
              autoComplete="new-password"
              required
            />
          </label>

          <label>
            Role
            <select
              value={newRole}
              onChange={(event) =>
                setNewRole(event.target.value as UserRole)
              }
            >
              <option value="STUDENT">Student</option>
              <option value="INSTRUCTOR">Instructor</option>
              <option value="ADMIN">Admin</option>
            </select>
          </label>
        </div>

        <button type="submit" disabled={creatingUser}>
          {creatingUser ? 'Creating...' : 'Create User'}
        </button>
      </form>

      <h3>Registered Users</h3>

      {usersLoading ? (
        <p>Loading users...</p>
      ) : users.length === 0 ? (
        <p>No users found.</p>
      ) : (
        <div className="admin-table-wrapper">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                <th>Created</th>
                <th>Actions</th>
              </tr>
            </thead>

            <tbody>
              {users.map((item) => (
                <tr key={item.id}>
                  <td>{item.name}</td>
                  <td>{item.email}</td>
                  <td>
                    <select
                      value={item.role}
                      aria-label={`Role for ${item.email}`}
                      onChange={(event) =>
                        void handleRoleChange(
                          item,
                          event.target.value as UserRole
                        )
                      }
                    >
                      <option value="STUDENT">Student</option>
                      <option value="INSTRUCTOR">Instructor</option>
                      <option value="ADMIN">Admin</option>
                    </select>
                  </td>
                  <td>
                    <span
                      className={
                        item.active
                          ? 'status-badge status-active'
                          : 'status-badge status-inactive'
                      }
                    >
                      {item.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td>
                    {item.createdAt
                      ? new Date(item.createdAt).toLocaleDateString()
                      : '—'}
                  </td>
                  <td>
                    <button
                      type="button"
                      onClick={() => void handleStatusChange(item)}
                    >
                      {item.active ? 'Deactivate' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );

  const renderAuditTab = () => (
    <section className="admin-section">
      <div className="section-heading">
        <div>
          <h2>Audit Logs</h2>
          <p>
            Review administrative changes and recorded exam submissions.
          </p>
        </div>

        <button
          type="button"
          onClick={() => void loadAuditLogs(auditPage)}
          disabled={auditLoading}
        >
          {auditLoading ? 'Refreshing...' : 'Refresh Logs'}
        </button>
      </div>

      {auditLoading ? (
        <p>Loading audit logs...</p>
      ) : auditLogs.length === 0 ? (
        <p>No audit logs found.</p>
      ) : (
        <div className="admin-table-wrapper">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>User</th>
                <th>Role</th>
                <th>Action</th>
                <th>Method</th>
                <th>Endpoint</th>
                <th>Status</th>
                <th>Details</th>
              </tr>
            </thead>

            <tbody>
              {auditLogs.map((log) => (
                <tr key={log.id}>
                  <td>
                    {log.timestamp
                      ? new Date(log.timestamp).toLocaleString()
                      : '—'}
                  </td>
                  <td>{log.userEmail || '—'}</td>
                  <td>{log.role || '—'}</td>
                  <td>{log.action || '—'}</td>
                  <td>{log.httpMethod || '—'}</td>
                  <td>{log.endpoint || '—'}</td>
                  <td>{log.httpStatus ?? '—'}</td>
                  <td>{log.details || '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div className="pagination-controls">
        <button
          type="button"
          disabled={auditLoading || auditPage <= 0}
          onClick={() => void loadAuditLogs(auditPage - 1)}
        >
          Previous
        </button>

        <span>
          Page {auditTotalPages === 0 ? 0 : auditPage + 1} of{' '}
          {auditTotalPages}
        </span>

        <button
          type="button"
          disabled={
            auditLoading ||
            auditTotalPages === 0 ||
            auditPage >= auditTotalPages - 1
          }
          onClick={() => void loadAuditLogs(auditPage + 1)}
        >
          Next
        </button>
      </div>
    </section>
  );

  const renderProctoringTab = () => (
    <section className="admin-section">
      <div className="section-heading">
        <div>
          <h2>Proctoring Incidents</h2>
          <p>
            Review candidate tab switches and fullscreen exits during exams.
          </p>
        </div>

        <button
          type="button"
          onClick={() => void loadIncidents()}
          disabled={incidentsLoading}
        >
          {incidentsLoading ? 'Refreshing...' : 'Refresh Incidents'}
        </button>
      </div>

      {incidentsLoading ? (
        <p>Loading incidents...</p>
      ) : incidents.length === 0 ? (
        <p>No proctoring incidents have been recorded.</p>
      ) : (
        <div className="admin-table-wrapper">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>Candidate</th>
                <th>Exam</th>
                <th>Attempt ID</th>
                <th>Incident</th>
              </tr>
            </thead>

            <tbody>
              {incidents.map((incident) => (
                <tr key={incident.id}>
                  <td>
                    {new Date(incident.occurredAt).toLocaleString()}
                  </td>
                  <td>{incident.studentEmail}</td>
                  <td>{incident.examTitle}</td>
                  <td>{incident.attemptId}</td>
                  <td>
                    {incident.type === 'TAB_SWITCH'
                      ? 'Tab Switch'
                      : 'Fullscreen Exit'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );

  return (
    <div className="admin-dashboard">
      <header className="admin-header">
        <div>
          <h1>Admin Dashboard</h1>
          <p>Manage users, monitor exams, and review system activity.</p>
        </div>

        <div className="admin-header-actions">
          <span>{user?.email}</span>
          <Link to="/">Home</Link>
          <button type="button" onClick={logout}>
            Logout
          </button>
        </div>
      </header>

      <nav className="admin-tabs" aria-label="Admin dashboard sections">
        <button
          type="button"
          className={activeTab === 'users' ? 'active' : ''}
          aria-pressed={activeTab === 'users'}
          onClick={() => {
            setError('');
            setSuccess('');
            setActiveTab('users');
          }}
        >
          User Management
        </button>

        <button
          type="button"
          className={activeTab === 'audit' ? 'active' : ''}
          aria-pressed={activeTab === 'audit'}
          onClick={() => {
            setError('');
            setSuccess('');
            setActiveTab('audit');
          }}
        >
          Audit Logs
        </button>

        <button
          type="button"
          className={activeTab === 'proctoring' ? 'active' : ''}
          aria-pressed={activeTab === 'proctoring'}
          onClick={() => {
            setError('');
            setSuccess('');
            setActiveTab('proctoring');
          }}
        >
          Proctoring
        </button>
      </nav>

      {error && (
        <div className="admin-alert admin-alert-error" role="alert">
          {error}
        </div>
      )}

      {success && (
        <div className="admin-alert admin-alert-success" role="status">
          {success}
        </div>
      )}

      <main className="admin-content">
        {activeTab === 'users' && renderUsersTab()}
        {activeTab === 'audit' && renderAuditTab()}
        {activeTab === 'proctoring' && renderProctoringTab()}
      </main>
    </div>
  );
}
