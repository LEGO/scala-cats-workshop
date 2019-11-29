const { readFileSync } = require('fs');
const path = require('path');

const LINE_SEPARATOR = '\n';
const FILE_REF_REGEX = /^FILE: (.+)$/;

const isFileReference = (line) => FILE_REF_REGEX.test(line);
const loadFileContent = (line, basePath) => {
    console.log(`path: ${basePath}, line: ${line}`)
    const actualPath = basePath ? basePath : "./"
    console.log(`path: ${actualPath}`)
    const filePath = line.match(FILE_REF_REGEX)[1];
    console.log(`filepath: ${filePath}`)
    return readFileSync(path.join(actualPath, filePath));
};

const preprocess = async (markdown, options) => {
    let res = markdown
        .split(LINE_SEPARATOR)
        .map(line => isFileReference(line) ? loadFileContent(line, options.includeDir) : line)
        .join(LINE_SEPARATOR);
    return await res
}

module.exports = preprocess;