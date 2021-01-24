const http = require('http');

const requestListener = function (req, res) {
  res.writeHead(200);
  res.write('Hello ');
  res.end(req.url.substring(1));
}

const server = http.createServer(requestListener);
server.listen(8080);