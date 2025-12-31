import { readFileSync } from 'fs';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';
import { execSync } from 'child_process';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// Read the config file to get the base path
const configPath = join(__dirname, '..', 'src', 'lib', 'config.ts');
const configContent = readFileSync(configPath, 'utf8');

// Extract the base path using regex
const basePathMatch = configContent.match(/basePath:\s*['"`]([^'"`]+)['"`]/);
const basePath = basePathMatch ? basePathMatch[1] : 'http://localhost:8080';

console.log(`Generating API from: ${basePath}/v3/api-docs`);

// Generate the API
const command = `openapi-generator-cli generate -i ${basePath}/v3/api-docs -g typescript-fetch -o src/lib/generated/api --additional-properties=typescriptThreePlus=true --auth admin:admin`;

try {
    execSync(command, { stdio: 'inherit', cwd: join(__dirname, '..') });
    console.log('API generation completed successfully!');
} catch (error) {
    console.error('API generation failed:', error.message);
    process.exit(1);
}
