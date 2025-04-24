import './style.css';
const urlParams = new URLSearchParams(window.location.search);
const paymentKey = urlParams.get("paymentKey");
const orderId = urlParams.get("orderId");
const amount = urlParams.get("amount");

const paymentKeyElement = document.getElementById("paymentKey");
const orderIdElement = document.getElementById("orderId");
const amountElement = document.getElementById("amount");

paymentKeyElement.textContent = paymentKey;
orderIdElement.textContent = orderId;
amountElement.textContent = `${amount}원`;

const confirmLoadingSection = document.querySelector('.confirm-loading');
const confirmSuccessSection = document.querySelector('.confirm-success');

async function confirmPayment() {
    // TODO: API를 호출해서 서버에게 paymentKey, orderId, amount를 넘겨주세요.
    // 서버에선 해당 데이터를 가지고 승인 API를 호출하면 결제가 완료됩니다.
    // https://docs.tosspayments.com/reference#%EA%B2%B0%EC%A0%9C-%EC%8A%B9%EC%9D%B8
    const response = await fetch('/sandbox-dev/api/v1/payments/confirm', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            paymentKey,
            orderId,
            amount
        }),
    });

    if (response.ok) {
        confirmLoadingSection.style.display = 'none';
        confirmSuccessSection.style.display = 'flex';
    }
}

const confirmPaymentButton = document.getElementById('confirmPaymentButton');
confirmPaymentButton.addEventListener('click', confirmPayment);