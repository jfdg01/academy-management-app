import type { PageLoad } from './$types';

export const load: PageLoad = ({ url }: { url: URL }) => {
	// Pagination
	const page = parseInt(url.searchParams.get('page') ?? '0', 10);
	const size = parseInt(url.searchParams.get('size') ?? '10', 10);
	const sortBy = url.searchParams.get('sortBy') ?? 'id';
	const sortDirection = (url.searchParams.get('sortDirection') ?? 'ASC').toUpperCase() as
		| 'ASC'
		| 'DESC';

	// Filters
	const searchMode = (url.searchParams.get('searchMode') ?? 'simple') as 'simple' | 'advanced';
	const q = url.searchParams.get('q') ?? undefined;
	const firstName = url.searchParams.get('firstName') ?? undefined;
	const lastName = url.searchParams.get('lastName') ?? undefined;
	const dni = url.searchParams.get('dni') ?? undefined;
	const email = url.searchParams.get('email') ?? undefined;
	const enabledParam = url.searchParams.get('enabled');
	const enabled = enabledParam === 'true' ? true : enabledParam === 'false' ? false : undefined;

	return {
		pagination: {
			page,
			size,
			sortBy,
			sortDirection
		},
		filters: {
			searchMode,
			q,
			firstName,
			lastName,
			dni,
			email,
			enabled
		}
	};
};
