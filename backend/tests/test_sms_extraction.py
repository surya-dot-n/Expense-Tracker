from datetime import datetime

from sms.schemas import SMSCreate
from sms.service import extract_transaction

def test_expense_sms():
    sms = SMSCreate(
        sender="SBI",
        message="Rs.500 debited from your account at SWIGGY",
        received_at=datetime.now()
    )

    result  = extract_transaction(sms)

    print(result)

    assert result.amount == 500
    assert result.transaction_type == "Expense"

    def test_income_sms():
        sms = SMSCreate(
            sender="HDFC",
            message="INR 2000 credited to your account",
            received_at=datetime.now()
        )

        result = extract_transaction(sms)

        print(result)

        assert result.amount == 2000
        assert result.transaction_type == "income"
    