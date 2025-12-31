import { deLocalizeUrl } from '$lib/paraglide/runtime';
import type { RequestEvent } from '@sveltejs/kit';

export const reroute = (request: RequestEvent) => deLocalizeUrl(request.url).pathname;

// For SvelteKit's internal client-side requests
export const transport = ({
	request,
	fetch
}: {
	request: Request;
	fetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>;
}) => {
	return fetch(request);
};
