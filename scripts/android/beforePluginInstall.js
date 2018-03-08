#!/usr/bin/env node

'use strict';

const fs = require('fs');
const _ = require('lodash');
const utilities = require("../lib/utilities");

module.exports = function(context) {

  var androidResPath = utilities.getAndroidResPath(context);

  if (androidResPath !== null) {

    const stylesPath = androidResPath + '/res/values/styles.xml';
    const colorPath = androidResPath + '/res/values/color.xml';

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
  }
};
