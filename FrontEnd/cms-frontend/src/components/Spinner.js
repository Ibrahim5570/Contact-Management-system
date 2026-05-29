import React from 'react';

function Spinner({ size = 10, color = 'blue' }) {
    return (
        <span style={{
            display: 'inline-block',
            width: size,
            height: size,
            border: `2px solid ${color}33`,
            borderTop: `2px solid ${color}`,
            borderRadius: '50%',
            animation: 'spin 0.7s linear infinite',
            marginRight: '8px',
            verticalAlign: 'middle',
        }} />
    );
}

export default Spinner;