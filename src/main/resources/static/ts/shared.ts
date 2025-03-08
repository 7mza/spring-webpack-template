import '../scss/styles.scss';

import { Toast } from 'bootstrap';

export function sayHi(): string {
  return `Hi there! it's ${getCurrentTime()}!`;
}

function getCurrentTime(): string {
  return new Date().toLocaleTimeString();
}

interface ToastData {
  message: string;
  theme?: string;
}

export function showToast(data: ToastData): void {
  document.querySelectorAll('.toast').forEach((toast) => toast.remove());
  const toastContainer = document.getElementById('toast-container');
  if (!toastContainer) {
    console.error('Toast container not found!');
    return;
  }
  const toast = document.createElement('div');
  const themeClass = data.theme || 'text-bg-danger';
  toast.className = `toast align-items-center ${themeClass} border-0`;
  toast.setAttribute('role', 'alert');
  toast.setAttribute('aria-live', 'assertive');
  toast.setAttribute('aria-atomic', 'true');
  toast.setAttribute('data-bs-delay', '10000');
  const flexDiv = document.createElement('div');
  flexDiv.className = 'd-flex';
  const toastBody = document.createElement('div');
  toastBody.className = 'toast-body wrap p-3';
  toastBody.textContent = data.message;
  const closeButton = document.createElement('button');
  closeButton.setAttribute('type', 'button');
  closeButton.className = 'btn-close me-2 m-auto';
  closeButton.setAttribute('data-bs-dismiss', 'toast');
  closeButton.setAttribute('aria-label', 'Close');
  flexDiv.appendChild(toastBody);
  flexDiv.appendChild(closeButton);
  toast.appendChild(flexDiv);
  toastContainer.appendChild(toast);
  const bsToast = new Toast(toast);
  bsToast.show();
}
