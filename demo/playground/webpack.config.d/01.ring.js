const ringConfig = require('@jetbrains/ring-ui/webpack.config').config;
const path = require('path');

config.module.rules.push(...ringConfig.module.rules)

config.module.rules.push(
    {
        test: /\.css$/,
        exclude: [
            path.resolve(__dirname, "../../node_modules/@jetbrains/ring-ui")
        ],
        use: [
            {
                loader: 'style-loader',
                options: {}
            },
            {
                loader: 'css-loader',
                options: {}
            }
        ]
    }
)