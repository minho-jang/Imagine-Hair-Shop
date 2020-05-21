# -*- encoding: utf8 -*-
import base64
from flask import Flask, render_template, send_file
from flask_restful import Api
from paramiko import SSHClient
import time
import os
from dlib import get_frontal_face_detector as face_detector
from dlib import shape_predictor as shape_predictor
import cv2
import numpy as np
import skimage
from skimage.draw import polygon
from scipy.spatial import ConvexHull
from celery import Celery

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


root_dir = 'ROOT_DIR'
source_img_dir = os.path.join(root_dir, 'source-image')
target_img_dir = os.path.join(root_dir, 'target-image')
result_img_dir = os.path.join(root_dir, 'result-image')


def face_swap(origin_path, gan_path, outpath):
    predictor = shape_predictor('shape_predictor_68_face_landmarks.dat')

    # image load & detect landmarks
    img = cv2.imread(origin_path)
    faces = face_detector()(img)[0]
    shape = predictor(img, faces)
    landmarks = np.array([[p.x, p.y] for p in shape.parts()])
    outline = landmarks[[*range(17), *range(26, 16, -1)]]

    # draw convexhull
    Y, X = polygon(outline[:, 1], outline[:, 0])
    cropped_img = np.zeros(img.shape, dtype=np.uint8)
    cropped_img[Y, X] = img[Y, X]

    vertices = ConvexHull(landmarks).vertices
    Y, X = skimage.draw.polygon(landmarks[vertices, 1], landmarks[vertices, 0])
    cropped_img = np.zeros(img.shape, dtype=np.uint8)

    # face swap
    img2 = cv2.imread(gan_path)
    img2[Y, X] = img[Y, X]

    cv2.imwrite(outpath, img2)

def swap(source_img,target_img,result_img):  # Do face swap & return result image
    image_info = {}

    image_info['src'] = source_img
    image_info['dst'] = target_img
    image_info['out'] = result_img
    image_info['warp_2d'] = False
    image_info['correct_color'] = True
    image_info['no_debug_window'] = True

    face_swap(source_img, target_img, result_img)

    return result_img

@app.route('/')
def f1():
    ## 스왑만 하고싶을때 쓰삼

    # img_name = os.path.join('SERVER_DIR', 'FaceSwap/source-image/111.png')
    # tar_name = os.path.join('SERVER_DIR', 'FaceSwap/target-image/111.png')
    # ret_name = os.path.join('SERVER_DIR','result2.png')
    #
    # swap(img_name, tar_name, ret_name)
    #
    # return send_file('result2.png', mimetype='image/png')
    return render_template('index.html')

@app.route('/photo/<img_name>', methods=['POST'])
def start_synthesis(img_name):
    result = execute_gpu.delay(img_name)
    print('1111')
    return 'plz'

@app.route('/result/<img_name>', methods=['POST'])
def get_result(img_name):
    print('result is returned')

@celery.task
def execute_gpu(img_name):
    client = SSHClient()
    client.load_system_host_keys()
    client.connect(host, username=user, port=port, password=password)

    ############################################################
    # b64_string = request.form.get('image')

    # source_img = os.path.join(source_img_dir, img_name + '.jpg')
    # target_img = os.path.join(target_img_dir, img_name + '_target.jpg')
    # result_img = os.path.join(result_img_dir, img_name + '_result.jpg')

    # with open(source_img, "wb") as f:
    #     f.write(base64.b64decode(b64_string))
    #
    ############################################################

    # 테스트 코드
    ############################################################

    source_img = os.path.join(source_img_dir, img_name + '.png')
    target_img = os.path.join(target_img_dir, img_name + '_target.png')
    result_img = os.path.join(result_img_dir, img_name + '_result.png')

    #################################################################

    #### GPU서버에 사진 요청 ####

    start = time.time()

    sftp = client.open_sftp()
    sftp.put(source_img, STYLEGAN_FFHQ_DIR + source_img)

    stdin, stdout, stderr = client.exec_command(RUN_COMMAND)

    for i in stderr.readlines():
        print(i)

    for i in stdout.readlines():
        print(i)

    # target image 는 현재 경로에 일단 저장
    sftp.get(STYLEGAN_RESULT_IMAGE_PATH, target_img)

    print("*** get ")
    swap(source_img, target_img, result_img)

    client.close()
    sftp.close()

    with open(result_img, "rb") as img:
        enc_str = base64.b64encode(img.read())

    print('time ****', time.time() - start)

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
