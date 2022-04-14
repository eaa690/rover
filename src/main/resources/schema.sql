CREATE TABLE `ROVER` (
    `ID` int(11) NOT NULL AUTO_INCREMENT,
    `NAME` varchar(50) NOT NULL,
    `PASSCODE` varchar(4) NOT NULL,
    PRIMARY KEY (`ID`)
);

CREATE TABLE `PICTURE` (
    `ID` int(11) NOT NULL AUTO_INCREMENT,
    `URL` varchar(255) NOT NULL,
    `FILENAME` varchar(50) NOT NULL,
    `ROVER_ID` int(11) NOT NULL,
    `TIMESTAMP` datetime NOT NULL,
    PRIMARY KEY (`ID`)
);