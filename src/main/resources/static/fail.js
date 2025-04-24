import './style.css';
const urlParams = new URLSearchParams(window.location.search);
const errorCode = urlParams.get("code");
const errorMessage = urlParams.get("message");

const errorCodeElement = document.getElementById("error-code");
const errorMessageElement = document.getElementById("error-message");

errorCodeElement.textContent = errorCode;
errorMessageElement.textContent = errorMessage;