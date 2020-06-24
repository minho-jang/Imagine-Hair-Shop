#! /usr/bin/env python
import os
import cv2
import argparse

from FaceSwap.face_detection import select_face
from FaceSwap.face_swap import face_swap

def faceswap(args):
    src_img = cv2.imread(args['src'])
    dst_img = cv2.imread(args['dst'])

    #얼굴 선택
    src_points, src_shape, src_face = select_face(src_img)
    dst_points, dst_shape, dst_face = select_face(dst_img)

    output = face_swap(src_face, dst_face, src_points, dst_points, dst_shape, dst_img, args)

    dir_path = os.path.dirname(args['out'])
    if not os.path.isdir(dir_path):
        os.makedirs(dir_path)

    cv2.imwrite(args['out'], output)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='FaceSwapApp')
    parser.add_argument('--src', required=True, help='Path for source image')
    parser.add_argument('--dst', required=True, help='Path for target image')
    parser.add_argument('--out', required=True, help='Path for storing output images')
    parser.add_argument('--warp_2d', default=False, action='store_true', help='2d or 3d warp')
    parser.add_argument('--correct_color', default=False, action='store_true', help='Correct color')
    parser.add_argument('--no_debug_window', default=False, action='store_true', help='Don\'t show debug window')
    args = parser.parse_args()

    faceswap(args)
