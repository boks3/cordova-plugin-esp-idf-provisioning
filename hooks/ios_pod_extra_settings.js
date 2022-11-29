var fs = require('fs');

var podExtraSettings = `
ENV['SWIFT_VERSION'] = '5'
`;

fs.appendFile('platforms/ios/Podfile', podExtraSettings, function (err, data) {
  if (err) {
    console.log(err);
    process.exit(1);
  }
});
