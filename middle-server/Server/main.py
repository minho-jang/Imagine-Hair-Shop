# -*- encoding: utf8 -*-
import io
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
from skimage import data
from skimage import exposure
from PIL import Image, ImageEnhance

# celery 동작 명령어
# celery worker -A main.celery --loglevel=INFO --pool=solo

#셀러리 객체를 만들어서 셀러리 객체에 비동기처리를 진행한다.
def make_celery(app):
    celery = Celery(
        "main",
        backend=app.config['CELERY_RESULT_BACKEND'],
        broker=app.config['CELERY_BROKER_URL']
    )

    class ContextTask(celery.Task):
        def __call__(self, *args, **kwargs):
            with app.app_context():
                return self.run(*args, **kwargs)

    celery.Task = ContextTask
    return celery

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


root_dir = ROOT_DIR
#root_dir = '/tmp'

source_img_dir = os.path.join(root_dir, 'source-image')
target_img_dir = os.path.join(root_dir, 'target-image')
result_img_dir = os.path.join(root_dir, 'result-image')

#PIL image를 numpy 형태로
def PIL2numpy(image):
    return np.asarray(image)

def numpy2PIL(image):
    return Image.fromarray(np.uint8(image))

# 이미지를 밝게 만들어준다.
def image_brighter(image, bright_factor=1.2):
    image = numpy2PIL(image)
    enhancer = ImageEnhance.Brightness(image)

    im_out = enhancer.enhance(bright_factor)
    im_out = PIL2numpy(im_out)
    return im_out

# Do face swap & return result image
def swap(source_img,target_img,result_img):

    image_info = {}

    image_info['src'] = source_img
    image_info['dst'] = target_img
    image_info['out'] = result_img

    face_main.faceswap(image_info)

    return result_img

#처음 전송받은 이미지 crop
@app.route('/crop/<img_name>', methods=['POST'])
def cropping(img_name):
    b64_string = request.form.get('image')
    post_request_image_bytes = base64.b64decode(b64_string)

    image = cv2.imdecode(np.frombuffer(post_request_image_bytes, np.uint8), -1)
    image = image_brighter(image)

    image_height, image_width = image.shape[:2]
    print("image height, width", image_height, image_width)

    #얼굴 탐지
    faces = face_detector()(image)

    if len(faces) == 0:
        print("얼굴 인식 실패!")
        return '0'

    face = faces[0]
    face_width = face.right()-face.left()
    face_height = face.bottom()-face.top()

    if(face_width < 250 or face_height < 250):
        #얼굴이 너무 작을 경우
        print("face width, height", face_width, face_height)
        print('얼굴이 작음')
        return '0'

    # ffhq image 얼굴 크기
    ffhq_face_size = 535

    # ffhq image frame [좌, 우, 상, 하 마진 길이]
    ffhq_image_frame = [225, 264, 344, 145]

    # 각 숫자는 ffhq의 data frame 값
    if (face_width >= 500):
        ffhq_face_size = 642
        ffhq_image_frame = [199, 183, 270, 112]

    #기존의 data set과 현재 이미지의 비율을 구한다.
    image_rate = face_width/ffhq_face_size
    print("face_width", face_width)
    print("ffhq_image_fram", ffhq_image_frame)
    result_image_frame = [_*image_rate for _ in ffhq_image_frame]
    print("image_rate", image_rate)
    print("margin list", result_image_frame)

    # result image frame [좌, 우, 상, 하 마진 길이]
    result_image_left = int(face.left() - result_image_frame[0])
    result_image_right = int(face.right() + result_image_frame[1])
    result_image_top = int(face.top() - result_image_frame[2])
    result_image_bottom = int(face.bottom() + result_image_frame[3])

    if result_image_top < 0:
        result_image_top = 0

    #얼굴을 주위로 margin이 부족한지 파악한다.
    if (result_image_left < 0 or result_image_top < 0 or
            result_image_right > image_width or result_image_bottom > image_height):
        # 얼굴이 너무 한쪽에 치우쳐 있을 때
        if result_image_left < 0:
            print('left 가 0보다 작음')
        if result_image_top < 0:
            print('top 이 0보다 작음')
        if result_image_right > image_width:
            print('right가 width 보다 큼')
        if result_image_bottom > image_height:
            print('bottom 이 heigt 보다 큼')
        return '0'

    #해당 margin 비율에 맞게 자르고 upsampling 진행
    result_image = image[result_image_top:result_image_bottom, result_image_left:result_image_right]
    result_image = cv2.resize(result_image, (1024, 1024), interpolation=cv2.INTER_CUBIC)

    cv2.imwrite(os.path.join(root_dir, 'testing_image.jpg'), result_image)

    #image base64 encoding
    retval, buffer = cv2.imencode('.jpg', result_image)
    result_image_base64 = base64.b64encode(buffer)

    return result_image_base64


@app.route('/start/<img_name>', methods=['POST'])
def start_synthesis(img_name):

    ## 테스트할 때는 주석 처리
    ##########################################################
    b64_string = request.form.get('image')

    source_img = os.path.join(source_img_dir, img_name + '.jpg')

    with open(source_img, "wb") as f:
        f.write(base64.b64decode(b64_string))

    print("gpu run command")
    task = execute_gpu.delay(img_name)


    print('post return ', task.id)
    return task.id

# 안드로이드 상에서 결과 이미지 확인을 하기 위한 post 함수. 이미지 존재 시 이미지 인코딩 문자열 리턴, 없을 시 '0' 리턴
@app.route('/result/<img_name>', methods=['POST','GET'])
def get_result(img_name):
    if os.path.isfile(os.path.join(result_img_dir, img_name+'.png')):
        with open(os.path.join(result_img_dir, img_name+'.png'), "rb") as img:
            enc_str = base64.b64encode(img.read())
        print('task completed')
        os.remove(os.path.join(result_img_dir, img_name+'.png'))
        return enc_str
    return '0'


# gpu 서버 접속, 실행하는 함수
@celery.task
def execute_gpu(img_name):
    client = SSHClient()
    client.load_system_host_keys()
    print("gpu server connection..")
    client.connect(host, username=user, port=port, password=password)
    print("gpu server connection Ok.")
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
