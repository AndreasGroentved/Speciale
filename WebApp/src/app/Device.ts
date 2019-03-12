export class Device {
  public ip: string;
  public id: string;

  public Device(id: string, ip: string) {
    this.id = id;
    this.ip = ip;
  }
}
