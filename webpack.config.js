'use strict';

import path from 'path';
import { fileURLToPath } from 'url';
import { dirname } from 'path';
import MiniCssExtractPlugin from 'mini-css-extract-plugin';
import CssMinimizerPlugin from 'css-minimizer-webpack-plugin';
import TerserPlugin from 'terser-webpack-plugin';
import autoprefixer from 'autoprefixer';
import { PurgeCSSPlugin } from 'purgecss-webpack-plugin';
import { glob } from 'glob';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

export default {
  entry: [
    /* 'core-js', 'regenerator-runtime/runtime',*/ './src/main/resources/static/ts/main.ts',
  ],
  output: {
    path: path.resolve(__dirname, './src/main/resources/static/dist/'),
    filename: 'bundle.min.js',
    clean: true,
  },
  module: {
    rules: [
      {
        test: /\.ts$/,
        exclude: /node_modules/,
        use: [
          {
            loader: 'babel-loader',
            options: {
              presets: [
                ['@babel/preset-env', { useBuiltIns: 'entry', corejs: 3 }],
              ],
            },
          },
          { loader: 'ts-loader', options: { transpileOnly: true } },
        ],
      },
      {
        test: /\.(scss)$/,
        use: [
          {
            loader: MiniCssExtractPlugin.loader,
          },
          {
            loader: 'css-loader',
          },
          {
            loader: 'postcss-loader',
            options: {
              postcssOptions: {
                plugins: [autoprefixer],
              },
            },
          },
          {
            loader: 'sass-loader',
          },
        ],
      },
      {
        test: /\.(woff|woff2|eot|ttf|svg)$/,
        type: 'asset/resource',
        generator: {
          filename: '[name][ext]',
        },
      },
    ],
  },
  resolve: {
    extensions: ['.ts', '.js', '.scss'],
  },
  plugins: [
    new MiniCssExtractPlugin({ filename: 'bundle.min.css' }),
    new PurgeCSSPlugin({
      paths: [
        ...glob.sync(
          path.join(__dirname, './src/main/resources/templates/**/*.html'),
          { nodir: true }
        ),
        ...glob.sync(
          path.join(__dirname, './src/main/resources/static/**/*.{js,ts}'),
          { nodir: true }
        ),
      ],
    }),
  ],
  optimization: {
    minimize: true,
    minimizer: [new TerserPlugin(), new CssMinimizerPlugin()],
  },
  mode: 'production',
  devtool: 'source-map',
};
