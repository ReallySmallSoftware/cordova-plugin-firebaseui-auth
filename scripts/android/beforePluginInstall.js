#!/usr/bin/env node

'use strict';

const fs = require('fs');
const _ = require('lodash');

module.exports = function(context) {
  const stylesPath = context.opts.projectRoot + '/platforms/android/res/values/styles.xml';
  const colorPath = context.opts.projectRoot + '/platforms/android/res/values/color.xml';

  function fileExistsWithContent(filename) {
    if (!fs.existsSync(filename)) {
      console.log(filename + " does not exist.");
      return false;
    }
    const stats = fs.statSync(filename);
    const fileSizeInBytes = stats.size;

    if (fileSizeInBytes == 0) {
      console.log(filename + " is empty.");
    }

    return true;
  }

  if (!fileExistsWithContent(stylesPath)) {
    fs.writeFileSync(stylesPath, "<resources></resources>");
    console.log("Added styles definition.");
  }

  if (!fileExistsWithContent(colorPath)) {
    fs.writeFileSync(colorPath, "<resources></resources>");
    console.log("Added colours.");
  }
};
