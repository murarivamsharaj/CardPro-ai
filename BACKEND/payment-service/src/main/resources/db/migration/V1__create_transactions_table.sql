CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    rzp_order_id VARCHAR(100) UNIQUE,
    rzp_payment_id VARCHAR(100),
    amount DECIMAL(10, 2) NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    item_details JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE UNIQUE INDEX idx_transactions_rzp_order_id ON transactions(rzp_order_id);
CREATE INDEX idx_transactions_status ON transactions(status);
