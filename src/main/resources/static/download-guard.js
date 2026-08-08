(() => {
  const SELECTOR = 'a[data-halo-protected-download="true"]';
  const MESSAGE = '请登录后下载';

  document.addEventListener(
    'click',
    async (event) => {
      const target = event.target;
      if (!(target instanceof Element)) return;

      const link = target.closest(SELECTOR);
      if (!(link instanceof HTMLAnchorElement)) return;

      event.preventDefault();
      if (link.dataset.permissionChecking === 'true') return;
      link.dataset.permissionChecking = 'true';

      try {
        const checkUrl = new URL(link.href, window.location.href);
        checkUrl.searchParams.set('check', '1');
        const response = await fetch(checkUrl, {
          method: 'GET',
          credentials: 'same-origin',
          redirect: 'manual',
          headers: { Accept: 'application/json' },
        });

        if (response.status === 204) {
          window.location.assign(link.href);
          return;
        }
      } catch {
        // Network/authentication failures use the same user-facing message as
        // denied requests so no permission internals are leaked to visitors.
      } finally {
        delete link.dataset.permissionChecking;
      }

      window.alert(MESSAGE);
    },
    true,
  );
})();
