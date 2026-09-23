import React from 'react';

/**
 * Display error alert with field-level details if available
 */
function ErrorMessage({ message, details }) {
  if (!message && !details) return null;

  return (
    <div className="alert alert-error">
      <p>{message}</p>
      {details && Array.isArray(details) && details.length > 0 && (
        <ul style={{ marginTop: '0.5rem', marginLeft: '1.5rem' }}>
          {details.map((detail, idx) => (
            <li key={idx}>
              {detail.field}: {detail.error}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default ErrorMessage;
