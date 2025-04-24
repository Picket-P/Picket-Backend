import './style.css';
import { loadTossPayments, ANONYMOUS } from "@tosspayments/tosspayments-sdk";

const amount = {
    currency: "KRW",
    value: 300,
};

const main = async () => {
    const tossPayments = await loadTossPayments(
        "test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm"
    );
    const widgets = tossPayments.widgets({
        customerKey: ANONYMOUS
    })

    /**
     * 위젯의 결제금액을 결제하려는 금액으로 초기화하세요.
     * renderPaymentMethods, renderAgreement, requestPayment 보다 반드시 선행되어야 합니다.
     * @docs https://docs.tosspayments.com/sdk/v2/js#widgetssetamount
     */
    await widgets.setAmount(amount);

    await Promise.all([
        /**
         * 결제창을 렌더링합니다.
         * @docs https://docs.tosspayments.com/sdk/v2/js#widgetsrenderpaymentmethods
         */
        widgets.renderPaymentMethods({
            selector: "#payment-method",
            // 렌더링하고 싶은 결제 UI의 variantKey
            // 결제 수단 및 스타일이 다른 멀티 UI를 직접 만들고 싶다면 계약이 필요해요.
            // @docs https://docs.tosspayments.com/guides/v2/payment-widget/admin#새로운-결제-ui-추가하기
            variantKey: "DEFAULT",
        }),
        /**
         * 약관을 렌더링합니다.
         * @docs https://docs.tosspayments.com/reference/widget-sdk#renderagreement선택자-옵션
         */
        widgets.renderAgreement({ selector: "#agreement", variantKey: "AGREEMENT" }),
    ]);

    const paymentRequestButton = document.getElementById('payment-request-button');

    paymentRequestButton.addEventListener('click', async () => {
        try {
            /**
             * 결제 요청
             * 결제를 요청하기 전에 orderId, amount를 서버에 저장하세요.
             * 결제 과정에서 악의적으로 결제 금액이 바뀌는 것을 확인하는 용도입니다.
             * @docs https://docs.tosspayments.com/sdk/v2/js#widgetsrequestpayment
             */
            await widgets.requestPayment({
                orderId: generateRandomString(),
                orderName: "토스 티셔츠 외 2건",
                successUrl: window.location.origin + "/sandbox/success",
                failUrl: window.location.origin + "/sandbox/fail",
                customerEmail: "customer123@gmail.com",
                customerName: "김토스",
                customerMobilePhone: "01012341234",
            });
        } catch (err) {
            // TODO: 에러 처리
        }
    });
}

main();

const generateRandomString = () => window.btoa(Math.random()).slice(0, 20);