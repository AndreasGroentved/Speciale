exportclassProcuration {
  messageChainID:string;
deviceID: string;
recipientPublicKey: BigInteger;
dateFrom: Date;
dateTo: Date;


constructor(messageChainID: string, deviceID: string, recipientPublicKey: BigInteger, dateFrom: Date, dateTo: Date) {
    this.messageChainID = messageChainID;
    this.deviceID = deviceID;
    this.recipientPublicKey = recipientPublicKey;
    this.dateFrom = dateFrom;
    this.dateTo = dateTo;
  }
}
