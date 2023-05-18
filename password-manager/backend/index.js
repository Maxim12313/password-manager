const express = require("express");
const app = express();


app.get("/authenticate/:", function (req, res) {
    res.send("hi");
});


app.listen(3000, function () {
    console.log("Server is running on localhost3000");
});