import '../scss/styles.scss';

import { sayHi, showToast } from './shared';

(function listener() {
  const graphButton = document.getElementById(
    'btn'
  ) as HTMLButtonElement | null;
  if (graphButton) {
    graphButton.addEventListener('click', () =>
      showToast({
        message: `${sayHi()}`,
        theme: 'text-bg-primary',
      })
    );
  } else {
    console.error('button not found!');
  }
})();
