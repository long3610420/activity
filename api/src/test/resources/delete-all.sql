-- account 
delete from t_account;
delete from t_account_action;
delete from t_subaccount;
delete from t_clearing_subaccount;
delete from t_subaccount_transactions;

-- order 
delete from t_order;
delete from t_order_action;
delete from t_order_sequence;
delete from t_order_match_result;
delete from t_order_config;

-- clearing
delete from t_match_result;
delete from t_deposit_withdraw_result;
delete from t_clearing_transfer;
delete from t_clearing_config;

-- dw
delete from t_deposit_virtual;
delete from t_withdraw_virtual;
delete from t_deposit_withdraw_state_history;
delete from t_deposit_withdraw_clearing;
delete from t_withdraw_virtual_address;
delete from t_deposit_virtual_address;
delete from t_deposit_legal;
delete from t_withdraw_legal;
delete from t_finance_history;
delete from t_account_transfer;