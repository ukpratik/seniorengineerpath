// `npm run gate`: check all content without building the site.
import { gateNow } from '../src/content-tools/integration.ts';

const { ok, report } = gateNow();
console.log(report);
process.exit(ok ? 0 : 1);
