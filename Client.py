# Lester Lee and Brianna Rettig Python client
import xmlrpclib, sys

name = "http://"+sys.argv[1]+".cs.williams.edu:" + sys.argv[2]
server = xmlrpclib.Server(name)

def parseRequest(req):
    req = req.lower().split(" ")
    try:
        if "buy" in req:
            return server.bookstore.buy(int(req[1]))
        elif "lookup" in req:
            return server.bookstore.lookup(int(req[1]))
        elif "search" in req:
            return server.bookstore.search(" ".join(req[1:]))
        else:
            return "Invalid request!"
    except:
        return "Invalid request!"

print "You've walked into looks4books store for books"

while True:
    request = raw_input("> ")
    print parseRequest(request)

'''
for i in range(500):
    server.bookstore.search("college")
    server.bookstore.buy(53573)
'''
