const { readFileSync } = require('fs');
const path = require('path');

const LINE_SEPARATOR = '\n';
const FILE_REF_REGEX = /^FILE: (.+)$/;

const isFileReference = (line) => FILE_REF_REGEX.test(line);
const loadFileContent = (line, basePath) => {
    const filePath = line.match(FILE_REF_REGEX)[1];
    console.log(`path: ${path.join(basePath, filePath)}`)
    return readFileSync(path.join(basePath, filePath));
};

const preprocess = async (markdown, options) => {
    let res = markdown
        .split(LINE_SEPARATOR)
        .map(line => isFileReference(line) ? loadFileContent(line, options.includeDir) : line)
        .join(LINE_SEPARATOR);
    return await res
}

module.exports = preprocess;