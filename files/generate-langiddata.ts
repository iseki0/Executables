const lines = Deno.readTextFileSync('langid-240423.txt')
    .split('\n')
    .map(line => line.trim())
    .filter(line => line !== '');

const refEntries: string[] = [];
const rrefEntries: string[] = [];

for (const line of lines) {
    const [hex, ...rest] = line.split(/\s+/);
    const comment = rest.join(' ');

    if (!hex || !comment) continue;

    // 跳过完全无效的项
    if (/unassigned|neither defined|temporarily assigned/i.test(comment)) {
        continue;
    }

    const tagMatch = comment.match(/^([a-zA-Z0-9\-]+)(,?\s*reserved)?$/);
    if (tagMatch) {
        const tag = tagMatch[1];
        const isReserved = /reserved/i.test(comment);
        const codeHex = `0x${parseInt(hex, 16).toString(16)}`;
        const codeKotlin = `${codeHex}.toUShort()`;

        const refLine = `${codeKotlin} to "${tag}"`;
        const rrefLine = `"${tag}" to ${codeKotlin}`;

        if (isReserved) {
            refEntries.push(`    // reserved: ${refLine},`);
            rrefEntries.push(`    // reserved: ${rrefLine},`);
        } else {
            refEntries.push(`    ${refLine},`);
            rrefEntries.push(`    ${rrefLine},`);
        }
    }
}

// 拼出完整 Kotlin 文件
const fileContent = `@file:JvmName("-LangIDData")

package space.iseki.executables.pe

import kotlin.jvm.JvmName

/*
https://learn.microsoft.com/en-us/openspecs/windows_protocols/ms-lcid/70feba9f-294e-491e-b6eb-56532684c37f
[MS-LCID]:
Windows Language Code Identifier (LCID) Reference

16.0

4/23/2024
*/
// @formatter:off

internal val LangID_rref = mapOf(
${rrefEntries.join('\n')}
)

internal val LangID_ref = mapOf(
${refEntries.join('\n')}
)
`;

Deno.writeTextFileSync("src/commonMain/kotlin/space/iseki/executables/pe/LangIDData.kt", fileContent);
console.log("✅ Kotlin file written to LangIDData.kt");
