export class Device {
  public ip: string;
  public id: string;

  public Device(ip: string, id: string) {
    this.id = id;
    this.ip = ip;
  }
}
