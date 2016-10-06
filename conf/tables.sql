CREATE TABLE `wma_taskactivity_detail_table` (
  `wtad_id` int(11) NOT NULL AUTO_INCREMENT,
  `wtat_id` int(11) DEFAULT NULL,
  `subject` text,
  `from` varchar(255) DEFAULT NULL,
  `senderdomain` varchar(255) DEFAULT NULL,
  `link` varchar(255) DEFAULT NULL,
  `processingTime` int(11) DEFAULT NULL,
  PRIMARY KEY (`wtad_id`)
) ENGINE=InnoDB AUTO_INCREMENT=882235 DEFAULT CHARSET=latin1;

CREATE TABLE `wma_taskactivity_table` (
  `wtat_id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) DEFAULT NULL,
  `pass` varchar(50) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `emailprovider` varchar(50) DEFAULT NULL,
  `failMessage` varchar(255) DEFAULT NULL,
  `spamcount` int(11) DEFAULT NULL,
  `mailreadcount` int(11) DEFAULT NULL,
  `processingTime` int(11) DEFAULT NULL,
  `proxyused` varchar(50) DEFAULT NULL,
  `audit_insert` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `proxyserver` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`wtat_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25190 DEFAULT CHARSET=latin1;

