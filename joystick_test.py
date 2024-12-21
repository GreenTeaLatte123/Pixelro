from pywinusb import hid
import time

# DHU-3300 BC 조이스틱의 VID와 PID를 설정합니다.
VID = 0x0ef5  # STMicroelectronics의 VID
PID = 0x3003  # DAHOON USB Gamekey의 PID

def read_from_joystick():
    """조이스틱에서 버튼 입력을 읽습니다."""
    try:
        # HID 장치 필터 생성
        filter_device = hid.HidDeviceFilter(vendor_id=VID, product_id=PID)
        devices = filter_device.get_devices()

        if not devices:
            print(f"VID: {VID:04x}, PID: {PID:04x} 장치를 찾을 수 없습니다.")
            return

        device = devices[0]
        device.open()

        def on_data_received(data):
            print(f"수신된 데이터: {data}")
            # 데이터에서 버튼 상태를 분석할 수 있습니다.
            # 여기서는 단순히 전체 데이터를 출력합니다.
            # 실제 버튼 상태를 분석하려면 데이터 구조를 이해해야 합니다.

        device.set_raw_data_handler(on_data_received)

        print(f"조이스틱 연결됨. VID: {VID:04x}, PID: {PID:04x}")

        while True:
            time.sleep(1)  # 데이터 읽기를 위해 대기

    except Exception as e:
        print(f"장치 열기 오류: {e}")

if __name__ == "__main__":
    read_from_joystick()  # 조이스틱에서 입력을 읽습니다.
