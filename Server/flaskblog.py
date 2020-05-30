# -*- encoding: utf8 -*-
import os
import base64
import cv2
import numpy as np
from flask import Flask, render_template
from flask_restful import Api, request
from paramiko import SSHClient
from FaceSwap import face_main
from celery import Celery
from dlib import get_frontal_face_detector as face_detector


# celery 동작 명령어
# celery worker -A flaskblog.celery --loglevel=INFO --pool=solo

def make_celery(app):
    celery = Celery(
        "flaskblog",
        backend=app.config['CELERY_RESULT_BACKEND'],
        broker=app.config['CELERY_BROKER_URL']
    )

    class ContextTask(celery.Task):
        def __call__(self, *args, **kwargs):
            with app.app_context():
                return self.run(*args, **kwargs)

    celery.Task = ContextTask
    return celery

# 기존 코드
app = Flask(__name__)

# celery를 위한 추가 코드
app.config.update(
    CELERY_BROKER_URL='redis://localhost:6379/0',
    CELERY_RESULT_BACKEND='redis://localhost:6379/0'
)
celery = make_celery(app)

api = Api(app)

host = "NIPA_GPU_SERVER_URL"
user = "NIPA_GPU_SERVER_USERNAME"
port ="NIPA_GPU_SERVER_PORT"
password = "NIPA_GPU_SERVER_PASSWORD"


#root_dir = ROOT_DIR
root_dir = '/tmp'

source_img_dir = os.path.join(root_dir, 'source-image')
target_img_dir = os.path.join(root_dir, 'target-image')
result_img_dir = os.path.join(root_dir, 'result-image')

def swap(source_img,target_img,result_img):  # Do face swap & return result image

    image_info = {}

    image_info['src'] = source_img
    image_info['dst'] = target_img
    image_info['out'] = result_img
    image_info['warp_2d'] = False
    image_info['correct_color'] = True
    image_info['no_debug_window'] = True

    face_main.faceswap(image_info)

    return result_img

# flask 내에서 테스트를 하기 위한 라우트. test.png 로 post 요청 보냄
@app.route('/')
def f1():
    return render_template('index.html')

## 스왑만 하고싶을때 쓰삼
@app.route('/swap/<img_name>')
def onlySwap(img_name):
    sour_name = os.path.join(source_img_dir, img_name + '.png')
    tar_name = os.path.join(target_img_dir, img_name + '.png')
    ret_name = os.path.join(result_img_dir, img_name + '.png')

    swap(sour_name, tar_name, ret_name)

    return render_template("swap.html",
                           sour_name='images/source-image/' + img_name + '.png',
                           tar_name='images/target-image/' + img_name + '.png',
                           ret_name='images/result-image/' + img_name + '.png')

#처음 전송받은 이미지 crop
@app.route('/crop/<img_name>', methods=['POST'])
def cropping(img_name):
    b64_string = request.form.get('image')
    image = base64.b64decode(b64_string)

    image = np.asarray(bytearray(image), dtype="uint8")
    image = cv2.imdecode(image, cv2.IMREAD_COLOR)

    faces = face_detector()(image)
    image_height, image_width = image.shape[:2]

    if(len(faces) != 1):
        # 얼굴이 하나가 아닐경우
        print('얼굴이 하나가 아님')
        return '0'

    face = faces[0]
    face_width = face.right()-face.left()
    face_height = face.bottom()-face.top()
    face_center = [(face.bottom()+face.top())/2, (face.right()+face.left())/2]

    if(face_width < 250 or face_height < 250):
        #얼굴이 너무 작을 경우
        print('얼굴이 작음')
        print(face_width, face_height)
        return '0'

    # ffhq image 얼굴 크기
    ffhq_face_size = 0

    # ffhq image frame [좌, 우, 상, 하 마진 길이]
    ffhq_image_frame = []

    # 변경할 비율
    image_change_rate = 0

    # 각 숫자는 ffhq의 data frame 값
    if (face_width >= 500):
        ffhq_face_size = 642
        ffhq_image_frame = [199, 183, 270, 112]
    else:
        ffhq_face_size = 535
        ffhq_image_frame = [225, 264, 344, 145]

    image_rate = face_width/ffhq_face_size
    result_image_frame = [_*image_rate for _ in ffhq_image_frame]

    # result image frame [좌, 우, 상, 하 마진 길이]
    result_image_left = int(face_center[1] - face_width / 2 - result_image_frame[0])
    result_image_right = int(face_center[1] + face_width / 2 - result_image_frame[1])
    result_image_top = int(face_center[0] - face_height / 2 - result_image_frame[2])
    result_image_bottom = int(face_center[0] + face_height / 2 - result_image_frame[3])

    if (result_image_left < 0 or result_image_top < 0 or
            result_image_right > image_width or result_image_bottom > image_height):
        # 얼굴이 너무 한쪽에 치우쳐 있을 때
        return '0'

    result_image = image[result_image_top:result_image_bottom, result_image_left:result_image_right]

    result_image = cv2.resize(result_image, (1024, 1024), interpolation=cv2.INTER_CUBIC)
    result_image_base64 = base64.b64encode(result_image)

    return result_image_base64


@app.route('/start/<img_name>', methods=['POST'])
def start_synthesis(img_name):

    ## 테스트할 때는 주석 처리
    ##########################################################
    b64_string = request.form.get('image')

    source_img = os.path.join(source_img_dir, img_name + '.jpg')

    with open(source_img, "wb") as f:
        f.write(base64.b64decode(b64_string))

    ##########################################################


    task = execute_gpu.delay(img_name)
    print('post return ', task.id)
    return task.id

# 안드로이드 상에서 결과 이미지 확인을 하기 위한 post 함수. 이미지 존재 시 이미지 인코딩 문자열 리턴, 없을 시 '0' 리턴
@app.route('/result/<img_name>', methods=['POST','GET'])
def get_result(img_name):
    if os.path.isfile(os.path.join(result_img_dir, img_name+'_result.png')):
        with open(os.path.join(result_img_dir, img_name+'.png'), "rb") as img:
            enc_str = base64.b64encode(img.read())
        print('task completed')
        os.remove(os.path.join(result_img_dir, img_name+'.png'))
        return enc_str
    print('task not completed')
    return '0'

# gpu 서버 접속, 실행하는 함수
@celery.task
def execute_gpu(img_name):
    client = SSHClient()
    client.load_system_host_keys()
    client.connect(host, username=user, port=port, password=password)
    sftp = client.open_sftp()

    source_img = os.path.join(source_img_dir, img_name + '.jpg')
    target_img = os.path.join(target_img_dir, img_name + '.png')
    result_img = os.path.join(result_img_dir, img_name + '.png')

    sftp.put(source_img, STYLEGAN_FFHQ_DIR + img_name)

    stdin, stdout, stderr = client.exec_command(RUN_COMMAND)

    # 오류 발생 시 출력
    for i in stderr.readlines():
        print(i)

    sftp.get(STYLEGAN_RESULT_IMAGE_PATH, target_img)

    swap(source_img, target_img, result_img)

    client.close()
    sftp.close()


if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
