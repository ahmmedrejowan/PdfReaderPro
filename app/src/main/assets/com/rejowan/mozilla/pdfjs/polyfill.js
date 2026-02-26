if (!URL.parse) {
   URL.parse = function (urlStr, base) {
      try {
        if (base) {
          return new URL(urlStr, base);
        }
        return new URL(urlStr);
      } catch (e) {
        return null;
      }
    };
}

if (!Promise.withResolvers) {
  Promise.withResolvers = function () {
    let resolve, reject;
    const promise = new Promise((res, rej) => {
      resolve = res;
      reject = rej;
    });
    return { promise, resolve, reject };
  };
}

if (!AbortSignal.any) {
  AbortSignal.any = function(signals) {
    const controller = new AbortController();

    const onAbort = () => controller.abort();
    for (const signal of signals) {
      if (signal.aborted) {
        controller.abort();
        break;
      }
      signal.addEventListener('abort', onAbort, { once: true });
    }

    return controller.signal;
  };
}
