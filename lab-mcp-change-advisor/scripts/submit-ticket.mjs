import {openAsBlob} from 'node:fs';
import {basename} from 'node:path';
import {fileURLToPath} from 'node:url';
import {parseArgs} from 'node:util';

async function submit() {
  const {values: args} = parseArgs({
    options: {
      url: {type: 'string', default: 'http://127.0.0.1:9090'},
      screen: {type: 'string'},
      title: {type: 'string'},
      request: {type: 'string'},
      attachment: {type: 'string', multiple: true, default: []}
    }
  });

  if (!args.screen || !args.title || !args.request || args.attachment.length > 3) {
    throw new Error('Provide --screen, --title, --request and up to three --attachment paths.');
  }
  const server = new URL(args.url);
  if (!['http:', 'https:'].includes(server.protocol) || server.username || server.password) {
    throw new Error('--url must be an HTTP(S) server address.');
  }

  const body = new FormData();
  body.set('screenPath', args.screen);
  body.set('title', args.title);
  body.set('request', args.request);
  for (const reference of args.attachment) {
    let path = reference;
    if (/^file:/i.test(reference)) {
      const fileUri = new URL(reference);
      if ((fileUri.hostname && fileUri.hostname !== 'localhost') || fileUri.search || fileUri.hash) {
        throw new Error('Use a file:// URL on this computer with no query or fragment.');
      }
      path = fileURLToPath(fileUri);
    }
    const file = await openAsBlob(path);
    if (file.size === 0 || file.size > 5 * 1024 * 1024) {
      throw new Error('Attachments must be nonempty and no larger than 5 MB.');
    }
    body.append('attachments', file, basename(path));
  }

  const send = openSession(server);
  const csrf = await csrfToken(send);
  const login = await send('/login', {
    method: 'POST',
    body: new URLSearchParams({
      username: 'biz',
      password: process.env.ADVISOR_BIZ_PASSWORD ?? '1234',
      [csrf.parameterName]: csrf.token
    })
  });
  const target = login.headers.get('location');
  await login.body?.cancel();
  if (login.status !== 302 || !target || new URL(target, server).pathname !== '/') {
    throw new Error('Business login failed. Check ADVISOR_BIZ_PASSWORD and the server address.');
  }
  const authenticatedCsrf = await csrfToken(send);
  const response = await send('/api/business/tickets', {
    method: 'POST',
    headers: {[authenticatedCsrf.headerName]: authenticatedCsrf.token},
    body
  });
  const result = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(result.detail || `Upload returned HTTP ${response.status}.`);
  }
  console.log(JSON.stringify(result, null, 2));
}

function openSession(server) {
  let cookie = '';
  return async (path, {headers = {}, ...options} = {}) => {
    const response = await fetch(new URL(path, server), {
      ...options,
      headers: cookie ? {...headers, Cookie: cookie} : headers,
      redirect: 'manual',
      signal: AbortSignal.timeout(30000)
    });
    const session = response.headers.getSetCookie().find((value) => value.startsWith('JSESSIONID='));
    if (session) cookie = session.split(';', 1)[0];
    return response;
  };
}

async function csrfToken(send) {
  const response = await send('/api/csrf');
  if (!response.ok) {
    throw new Error(`Could not open a login session (HTTP ${response.status}).`);
  }
  return response.json();
}

submit().catch((error) => {
  console.error(error.message);
  process.exitCode = 1;
});
