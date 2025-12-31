import { readFileSync, writeFileSync } from 'fs';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// Read the config file to get the base path
const configPath = join(__dirname, '..', 'src', 'lib', 'config.ts');
const configContent = readFileSync(configPath, 'utf8');

// Extract the base path using regex
const basePathMatch = configContent.match(/basePath:\s*['"`]([^'"`]+)['"`]/);
const basePath = basePathMatch ? basePathMatch[1] : 'http://localhost:8080';

// Read the openapitools.json file
const openapitoolsPath = join(__dirname, '..', 'openapitools.json');
const openapitoolsContent = JSON.parse(readFileSync(openapitoolsPath, 'utf8'));

// Update the inputSpec with the centralized base path
openapitoolsContent['generator-cli'].generators['typescript-fetch'].inputSpec = `${basePath}/v3/api-docs`;

// Write the updated content back
writeFileSync(openapitoolsPath, JSON.stringify(openapitoolsContent, null, 2));

console.log(`Updated openapitools.json with base path: ${basePath}`);
