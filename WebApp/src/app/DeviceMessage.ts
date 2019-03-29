export class DeviceMessage {
  text: string;
  timestamp: Date;
  messageID: string;

  constructor(text: string, timestamp: Date, messageID: string) {
    this.text = text;
    this.timestamp = timestamp;
    this.messageID = messageID;
  }

}
