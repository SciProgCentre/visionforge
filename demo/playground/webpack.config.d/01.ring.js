const ringConfig = require('@jetbrains/ring-ui/webpack.config').config;

config.module.rules.push(...ringConfig.module.rules)

config.module.rules.push(
    {
        test: /\.css$/,
        exclude: [
            'D:\\Work\\Projects\\visionforge\\build\\js\\node_modules\\@jetbrains\\ring-ui'
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